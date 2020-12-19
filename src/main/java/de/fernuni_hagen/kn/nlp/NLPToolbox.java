package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.analysis.HITS;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.db.neo4j.Neo4J;
import de.fernuni_hagen.kn.nlp.db.neo4j.Neo4JReader;
import de.fernuni_hagen.kn.nlp.db.neo4j.Neo4JWriter;
import de.fernuni_hagen.kn.nlp.file.ExternalResourcesExtractor;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.input.TikaDocumentConverter;
import de.fernuni_hagen.kn.nlp.math.WeightingFunctions;
import de.fernuni_hagen.kn.nlp.preprocessing.Preprocessor;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.Map;

/**
 * Main class of NLPToolbox.
 *
 * @author Nils Wende
 */
public class NLPToolbox {

	private final Config config;

	public NLPToolbox(final String configFile) {
		config = Config.fromJson(configFile);
		Neo4J.init(config);
	}

	private void run() {
		//writeAllInputToFreshDB();
		if (config.getAnalysis().pageRank()) {
			final var start = logStart("PageRank");
			final var pageRanks = new PageRank().calculate(new Neo4JReader(), WeightingFunctions.DICE);
			pageRanks.entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.limit(config.getAnalysis().getLimit())
					.forEach(System.out::println);
			logDuration("PageRank", start);
		}
		if (config.getAnalysis().hits()) {
			final var start = logStart("HITS");
			final var hits = new HITS().calculate(new Neo4JReader());
			hits.entrySet().stream()
					.sorted(Comparator.comparingDouble(e -> -e.getValue().getAuthorityScore()))
					.limit(config.getAnalysis().getLimit())
					.forEach(e -> System.out.println("Authority score of " + e.getKey() + ": " + e.getValue().getAuthorityScore()));
			hits.entrySet().stream()
					.sorted(Comparator.comparingDouble(e -> -e.getValue().getHubScore()))
					.limit(config.getAnalysis().getLimit())
					.forEach(e -> System.out.println("Hub score of " + e.getKey() + ": " + e.getValue().getHubScore()));
			logDuration("HITS", start);
		}
	}

	private void writeAllInputToFreshDB() {
		final var start = logStart("writeDB");
		final var db = new Neo4JWriter();
		db.deleteAll();
		final var documentConverter = new TikaDocumentConverter(config);
		final var preprocessor = Preprocessor.from(config);
		try (final var paths = Files.walk(config.getInputDir())) {
			paths.map(Path::toFile)
					.filter(File::isFile)
					.peek(db::addDocument)
					.map(documentConverter::convert)
					.flatMap(preprocessor::preprocess)
					.forEach(db::addSentence);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		} finally {
			if (!config.keepTempFiles()) {
				FileHelper.deleteTempFiles();
			}
			logDuration("writeDB", start);
		}
	}

	public static void main(final String[] args) {
		ExternalResourcesExtractor.extractExternalResources();
		final var configFile = parseCLI(args);
		new NLPToolbox(configFile).run();
		logCurrentThreadCpuTime();
	}

	private static String parseCLI(final String[] args) {
		final var options = new Options();
		final String configFile = "configFile";
		options.addOption(configFile, true, "path to the config file, default: " + Config.getDefaultConfigFilePath());
		final var parser = new DefaultParser();
		try {
			final var cli = parser.parse(options, args);
			return cli.getOptionValue(configFile);
		} catch (final ParseException e) {
			throw new UncheckedException(e);
		}
	}

	private static long logStart(final String name) {
		System.out.println("start " + name);
		return System.nanoTime();
	}

	private static void logDuration(final String name, final long start) {
		final var d = Duration.ofNanos(System.nanoTime() - start);
		System.out.println(String.format("%s duration: %d s %d ms", name, d.toSecondsPart(), d.toMillisPart()));
	}

	private static void logCurrentThreadCpuTime() {
		final var d = Duration.ofNanos(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime());
		System.out.println(String.format("main thread CPU time: %d m %d s %d ms", d.toMinutesPart(), d.toSecondsPart(), d.toMillisPart()));
	}

}

package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.analysis.Analysis;
import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.db.im.InMemoryReader;
import de.fernuni_hagen.kn.nlp.db.im.InMemoryWriter;
import de.fernuni_hagen.kn.nlp.file.ExternalResourcesExtractor;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.input.TikaDocumentConverter;
import de.fernuni_hagen.kn.nlp.preprocessing.Preprocessor;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.fernuni_hagen.kn.nlp.Logger.*;

/**
 * Main class of NLPToolbox.
 *
 * @author Nils Wende
 */
public class NLPToolbox {

	private final Config config;

	public NLPToolbox(final String configFile) {
		config = Config.fromJson(configFile);
		//Neo4J.init(config);
	}

	private void run() {
		writeAllInputToFreshDB();
		new Analysis(config.getAnalysisConfig(), new InMemoryReader()).analyze();
		//new CsvExporter().export();
		//new Neo4JReader().printPath("art", "version");
	}

	private void writeAllInputToFreshDB() {
		final var start = logStart("writeDB");
		final var db = new InMemoryWriter();
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

}

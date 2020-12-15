package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.db.neo4j.Neo4J;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.input.TikaDocumentConverter;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import de.fernuni_hagen.kn.nlp.workflow.Preprocessing;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main class of NLPToolbox.
 *
 * @author Nils Wende
 */
public class NLPToolbox {

	private final Config config;

	public NLPToolbox(final String configFile) {
		config = Config.fromJson(configFile);
	}

	private void run() {
		final var db = Neo4J.init(config);
		db.deleteAll();
		final var documentConverter = new TikaDocumentConverter(config);
		try (final var paths = Files.walk(config.getInputDir(), 1)) {
			paths.map(Path::toFile)
					.filter(File::isFile)
					.peek(db::addDocument)
					.map(documentConverter::convert)
					.flatMap(f -> new Preprocessing().preprocess(f))
					.forEach(db::addSentence);
			db.updateDiceAndCosts();
			db.getAllNodes().forEach(System.out::println);
			db.getAllRelationships().forEach(System.out::println);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		} finally {
			if (!config.keepTempFiles()) {
				FileHelper.deleteTempFiles();
			}
		}
	}

	public static void main(final String[] args) {
		final var configFile = parseCLI(args);
		new NLPToolbox(configFile).run();
		//TODO Workflow/Pipeline mit Builder und fromConfig
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

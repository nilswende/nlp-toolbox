package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.analysis.Analysis;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.config.JsonConfigParser;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import de.fernuni_hagen.kn.nlp.file.ExternalResourcesExtractor;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Preprocessor;
import de.fernuni_hagen.kn.nlp.preprocessing.textual.TikaDocumentConverter;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.IOException;
import java.nio.file.Files;

import static de.fernuni_hagen.kn.nlp.Logger.*;

/**
 * Main class of NLPToolbox.
 *
 * @author Nils Wende
 */
public class NLPToolbox {

	private final AppConfig config;

	public NLPToolbox(final JsonConfigParser configParser) {
		this.config = configParser.getAppConfig();
		DBFactory.init(this.config);
	}

	private void run() {
		if (!config.analysisOnly()) {
			clearDatabase();
			preprocess();
		}
		new Analysis(DBFactory.instance().getReader()).analyze();
		//new CsvExporter().export();
		//((Neo4JReader)DBFactory.instance().getReader()).printPath("art", "version");
	}

	private void clearDatabase() {
		final var db = DBFactory.instance().getWriter();
		db.deleteAll();
	}

	private void preprocess() {
		final var start = logStart("writeDB");
		final var db = DBFactory.instance().getWriter();
		db.deleteAll();
		final var documentConverter = new TikaDocumentConverter(null);
		final var preprocessor = Preprocessor.from(null);
		try (final var paths = Files.walk(config.getInputDir())) {
			paths.filter(p -> Files.isRegularFile(p))
					.peek(db::addDocument)
					.map(documentConverter::convert)
					.flatMap(preprocessor::preprocess)
					.forEach(db::addSentence);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		} finally {
			if (!false) {
				FileHelper.deleteTempFiles();
			}
			logDuration("writeDB", start);
		}
	}

	public static void main(final String[] args) {
		ExternalResourcesExtractor.extractExternalResources();
		new NLPToolbox(new JsonConfigParser(args)).run();
		logCurrentThreadCpuTime();
	}

}

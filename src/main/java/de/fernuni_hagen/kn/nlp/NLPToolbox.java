package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.analysis.Analysis;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.config.JsonConfigParser;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import de.fernuni_hagen.kn.nlp.file.ExternalResourcesExtractor;

import static de.fernuni_hagen.kn.nlp.Logger.logCurrentThreadCpuTime;

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
		new Analysis(DBFactory.instance().getReader()).analyze();
		//new CsvExporter().export();
		//((Neo4JReader)DBFactory.instance().getReader()).printPath("art", "version");
	}

	public static void main(final String[] args) {
		ExternalResourcesExtractor.extractExternalResources();
		new NLPToolbox(new JsonConfigParser(args)).run();
		logCurrentThreadCpuTime();
	}

}

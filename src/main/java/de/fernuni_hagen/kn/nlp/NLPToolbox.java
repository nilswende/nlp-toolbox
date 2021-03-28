package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.config.ConfigParser;
import de.fernuni_hagen.kn.nlp.config.JsonConfigParser;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.config.UseCaseConfig;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;

import java.util.List;

import static de.fernuni_hagen.kn.nlp.Logger.logCurrentThreadCpuTime;

/**
 * Main class of NLPToolbox.
 *
 * @author Nils Wende
 */
public class NLPToolbox {

	private final DBFactory dbFactory;
	private final AppConfig appConfig;
	private final List<UseCaseConfig> useCaseConfigs;

	public NLPToolbox(final ConfigParser configParser) {
		this(configParser.getAppConfig(), configParser.getUseCaseConfigs());
	}

	public NLPToolbox(final AppConfig appConfig, final List<UseCaseConfig> useCaseConfigs) {
		this.appConfig = appConfig;
		this.useCaseConfigs = useCaseConfigs;
		dbFactory = DBFactory.from(this.appConfig);
	}

	private void run() {
		final var dbReader = dbFactory.getReader();
		final var dbWriter = dbFactory.getWriter();
		useCaseConfigs.stream().map(UseCase::from).forEach(u -> u.execute(dbReader, dbWriter));
		//((Neo4JReader)DBFactory.instance().getReader()).printPath("art", "version");
	}

	public static void main(final String[] args) {
		//ExternalResourcesExtractor.extractExternalResources();
		new NLPToolbox(new JsonConfigParser(args)).run();
		logCurrentThreadCpuTime();
	}

}

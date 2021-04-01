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

	/**
	 * Creates an instance from a {@link ConfigParser}.
	 *
	 * @param configParser ConfigParser
	 */
	public NLPToolbox(final ConfigParser configParser) {
		this(configParser.getAppConfig(), configParser.getUseCaseConfigs());
	}

	/**
	 * Creates an instance from configs.
	 *
	 * @param appConfig      AppConfig
	 * @param useCaseConfigs UseCaseConfigs
	 */
	public NLPToolbox(final AppConfig appConfig, final List<UseCaseConfig> useCaseConfigs) {
		this.appConfig = appConfig;
		this.useCaseConfigs = useCaseConfigs;
		dbFactory = DBFactory.from(this.appConfig);
	}

	/**
	 * Run NLPToolbox with the supplied configs.
	 */
	public void run() {
		final var dbReader = dbFactory.getReader();
		final var dbWriter = dbFactory.getWriter();
		useCaseConfigs.stream().map(UseCase::from).forEach(u -> u.execute(dbReader, dbWriter));
	}

	public static void main(final String[] args) {
		new NLPToolbox(new JsonConfigParser(args)).run();
		logCurrentThreadCpuTime();
	}

}

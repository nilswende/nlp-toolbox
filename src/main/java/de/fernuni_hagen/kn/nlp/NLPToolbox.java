package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.config.ConfigParser;
import de.fernuni_hagen.kn.nlp.config.JsonConfigParser;
import de.fernuni_hagen.kn.nlp.config.UseCase;
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
	private final List<UseCase> useCases;

	/**
	 * Creates an instance from a {@link ConfigParser}.
	 *
	 * @param configParser ConfigParser
	 */
	public NLPToolbox(final ConfigParser configParser) {
		this(configParser.getAppConfig(), configParser.getUseCases());
	}

	/**
	 * Creates an instance from configs.
	 *
	 * @param appConfig AppConfig
	 * @param useCase   UseCase
	 */
	public NLPToolbox(final AppConfig appConfig, final UseCase useCase) {
		this(appConfig, List.of(useCase));
	}

	/**
	 * Creates an instance from configs.
	 *
	 * @param appConfig AppConfig
	 * @param useCases  UseCases
	 */
	public NLPToolbox(final AppConfig appConfig, final List<UseCase> useCases) {
		this.appConfig = appConfig;
		this.useCases = useCases;
		dbFactory = DBFactory.from(this.appConfig);
	}

	/**
	 * Run NLPToolbox with the supplied use cases.
	 */
	public void run() {
		final var dbReader = dbFactory.getReader();
		final var dbWriter = dbFactory.getWriter();
		useCases.forEach(useCase -> useCase.execute(dbReader, dbWriter));
	}

	public static void main(final String[] args) {
		final var nlpToolbox = new NLPToolbox(new JsonConfigParser(args));
		nlpToolbox.run();
		nlpToolbox.useCases.stream().map(UseCase::getResult).forEach(System.out::println);
		logCurrentThreadCpuTime();
	}

}

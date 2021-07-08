package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.config.parser.ConfigParser;
import de.fernuni_hagen.kn.nlp.config.parser.JsonConfigParser;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.util.List;
import java.util.Objects;

import static de.fernuni_hagen.kn.nlp.Logger.logCurrentThreadCpuTime;

/**
 * Hagen NLPToolbox is a collection of text mining tools.
 *
 * @author Nils Wende
 */
public class NLPToolbox {

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
	 * Creates an instance from use cases.
	 *
	 * @param appConfig AppConfig
	 * @param useCases  use cases
	 */
	public NLPToolbox(final AppConfig appConfig, final UseCase... useCases) {
		this(appConfig, List.of(useCases));
	}

	/**
	 * Creates an instance from use cases.
	 *
	 * @param appConfig AppConfig
	 * @param useCases  use cases
	 */
	public NLPToolbox(final AppConfig appConfig, final List<UseCase> useCases) {
		if (appConfig == null) {
			throw new IllegalArgumentException("missing AppConfig");
		}
		if (useCases == null || useCases.isEmpty() || useCases.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("missing use case in " + useCases);
		}
		this.appConfig = appConfig;
		this.useCases = useCases;
	}

	/**
	 * Runs the NLPToolbox with the supplied use cases.<br>
	 * After this, each use case will contain a {@link UseCase.Result} object which consequently contains that use case's results.
	 *
	 * @throws UncheckedException if any checked exception occurred. Catch at your own discretion
	 */
	public void run() {
		try (final var dbFactory = DBFactory.from(appConfig)) {
			final var dbReader = dbFactory.getReader();
			final var dbWriter = dbFactory.getWriter();
			useCases.forEach(useCase -> useCase.execute(appConfig, dbReader, dbWriter));
		}
	}

	/**
	 * Runs the NLPToolbox with the supplied JSON use case arguments and prints the results to the console.<br>
	 * See the {@link UseCase} subclasses for configuration.
	 * Generally, if a use case has a setter for a property, you can specify that property as a JSON attribute.<br>
	 * {@code new AnyUseCase().setProperty(value)} becomes {@code {"name": "AnyUseCase", "property": "value"}}.
	 *
	 * @param args JSON arguments
	 * @see JsonConfigParser
	 */
	public static void main(final String[] args) {
		final var nlpToolbox = new NLPToolbox(new JsonConfigParser(args));
		nlpToolbox.run();
		nlpToolbox.useCases.stream().map(UseCase::getResult).forEach(System.out::println);
		logCurrentThreadCpuTime();
	}

}

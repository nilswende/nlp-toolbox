package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
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

	/**
	 * Creates an instance with the default application-wide config.
	 */
	public NLPToolbox() {
		this(new AppConfig());
	}

	/**
	 * Creates an instance with a custom application-wide config.
	 *
	 * @param appConfig AppConfig
	 */
	public NLPToolbox(final AppConfig appConfig) {
		this.appConfig = Objects.requireNonNull(appConfig, "missing AppConfig");
	}

	/**
	 * Convenience method for calling {@link #run(List)}.
	 *
	 * @param useCases use cases
	 * @see #run(List)
	 */
	public void run(final UseCase... useCases) {
		run(List.of(useCases));
	}

	/**
	 * Runs the NLPToolbox with the supplied use cases.<br>
	 * After this, each use case will contain a {@link UseCase.Result} object which consequently contains that use case's results.
	 *
	 * @param useCases use cases
	 * @throws UncheckedException if any checked exception occurred. Catch at your own discretion
	 */
	public void run(final List<UseCase> useCases) {
		if (useCases == null || useCases.isEmpty() || useCases.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("missing use case in " + useCases);
		}
		try (final var dbFactory = DBFactory.from(appConfig)) {
			final var dbReader = dbFactory.getReader();
			final var dbWriter = dbFactory.getWriter();
			useCases.forEach(useCase -> useCase.execute(appConfig, dbReader, dbWriter));
		}
	}

	/**
	 * Runs the NLPToolbox with the supplied JSON use case arguments and prints the results to the console.<br>
	 * See the {@link UseCase} subclasses for configuration.
	 * Generally, if a use case has a setter for a property, you can specify that property as a JSON attribute:<br>
	 * {@code new AnyUseCase().setProperty(value)} becomes {@code {"name": "AnyUseCase", "property": "value"}}.
	 *
	 * @param args JSON arguments
	 * @see JsonConfigParser
	 */
	public static void main(final String[] args) {
		final var configParser = new JsonConfigParser(args);
		final var useCases = configParser.getUseCases();
		new NLPToolbox(configParser.getAppConfig()).run(useCases);
		useCases.stream().map(UseCase::getResult).forEach(System.out::println);
		logCurrentThreadCpuTime();
	}

}

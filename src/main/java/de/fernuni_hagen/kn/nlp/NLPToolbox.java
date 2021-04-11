package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;

import java.util.List;
import java.util.Objects;

/**
 * Main class of the Hagen NLPToolbox.
 *
 * @author Nils Wende
 */
public class NLPToolbox {

	private final DBFactory dbFactory;
	private final List<UseCase> useCases;

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
			throw new IllegalArgumentException("missing use case: " + useCases);
		}
		this.useCases = useCases;
		dbFactory = DBFactory.from(appConfig);
	}

	/**
	 * Runs the NLPToolbox with the supplied use cases.
	 * After this, each use case will contain a {@link UseCase.Result} object which consequently contains that use case's results.
	 */
	public void run() {
		final var dbReader = dbFactory.getReader();
		final var dbWriter = dbFactory.getWriter();
		useCases.forEach(useCase -> useCase.execute(dbReader, dbWriter));
	}

}

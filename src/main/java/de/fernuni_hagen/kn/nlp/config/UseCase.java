package de.fernuni_hagen.kn.nlp.config;

import java.util.Arrays;

/**
 * The use cases of this application.
 *
 * @author Nils Wende
 */
public enum UseCase {
	PAGERANK(PageRankConfig.class),
	HITS(HITSConfig.class);

	private final Class<? extends UseCaseConfig> configClass;

	UseCase(final Class<? extends UseCaseConfig> configClass) {
		this.configClass = configClass;
	}

	/**
	 * Returns the UseCase instance corresponding to the given String, ignoring case considerations.
	 *
	 * @param name the use case name
	 * @return UseCase
	 * @throws IllegalArgumentException if the given String does not match any UseCase
	 */
	public static UseCase fromIgnoreCase(final String name) {
		return Arrays.stream(values())
				.filter(v -> v.name().equalsIgnoreCase(name))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No use case with name " + name + " found"));
	}

	public Class<? extends UseCaseConfig> getConfigClass() {
		return configClass;
	}

}

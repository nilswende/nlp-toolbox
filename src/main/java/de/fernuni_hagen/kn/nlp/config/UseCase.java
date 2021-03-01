package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.analysis.BooleanRetrieval;
import de.fernuni_hagen.kn.nlp.analysis.CentroidBySpreadingActivation;

import java.util.Arrays;

/**
 * The use cases of this application.
 *
 * @author Nils Wende
 */
public enum UseCase {
	CLEAR_DATABASE(ClearDatabaseConfig.class),
	PREPROCESSING(PreprocessingConfig.class),
	BOOLEAN_RETRIEVAL(BooleanRetrieval.class),
	CENTROID_BY_SPREADING_ACTIVATION(CentroidBySpreadingActivation.class),
	DOCUMENT_SIMILARITY(DocSimConfig.class),
	HITS(HITSConfig.class),
	PAGE_RANK(PageRankConfig.class),
	TERM_SIMILARITY(TermSimConfig.class);

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
				.filter(v -> v.name().equalsIgnoreCase(name) || v.name().replace("_", "").equalsIgnoreCase(name))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No use case with name " + name + " found"));
	}

	public Class<? extends UseCaseConfig> getConfigClass() {
		return configClass;
	}

}

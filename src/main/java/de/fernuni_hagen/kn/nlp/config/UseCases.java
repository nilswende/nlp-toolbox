package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.analysis.BooleanRetrieval;
import de.fernuni_hagen.kn.nlp.analysis.CentroidBySpreadingActivation;
import de.fernuni_hagen.kn.nlp.analysis.DocumentSimilarity;
import de.fernuni_hagen.kn.nlp.analysis.HITS;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.analysis.TermSimilarity;
import de.fernuni_hagen.kn.nlp.db.ClearDatabase;
import de.fernuni_hagen.kn.nlp.preprocessing.Preprocessor;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The use cases of this application.
 *
 * @author Nils Wende
 */
public enum UseCases {
	CLEAR_DATABASE(ClearDatabase.Config.class),
	PREPROCESSING(Preprocessor.Config.class),
	BOOLEAN_RETRIEVAL(BooleanRetrieval.Config.class),
	CENTROID_BY_SPREADING_ACTIVATION(CentroidBySpreadingActivation.Config.class),
	DOCUMENT_SIMILARITY(DocumentSimilarity.Config.class),
	HITS(HITS.Config.class),
	PAGE_RANK(PageRank.Config.class),
	TERM_SIMILARITY(TermSimilarity.Config.class);

	private final Class<? extends UseCaseConfig> configClass;

	UseCases(final Class<? extends UseCaseConfig> configClass) {
		this.configClass = configClass;
	}

	/**
	 * Returns the UseCases instance corresponding to the given String, ignoring case considerations.
	 *
	 * @param name the use case name
	 * @return UseCases
	 * @throws IllegalArgumentException if the given String does not match any UseCases instance
	 */
	public static UseCases fromIgnoreCase(final String name) {
		return Arrays.stream(values())
				.filter(v -> v.name().equalsIgnoreCase(name) || v.name().replace("_", "").equalsIgnoreCase(name))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No use case with name '" + name + "' found. Possible use cases:" + System.lineSeparator()
						+ Arrays.stream(values()).map(Object::toString).collect(Collectors.joining(", "))));
	}

	public Class<? extends UseCaseConfig> getConfigClass() {
		return configClass;
	}

}

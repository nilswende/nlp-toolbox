package de.fernuni_hagen.kn.nlp.config.parser;

import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.analysis.BooleanRetrieval;
import de.fernuni_hagen.kn.nlp.analysis.CentroidByMinAvgDistance;
import de.fernuni_hagen.kn.nlp.analysis.CentroidBySpreadingActivation;
import de.fernuni_hagen.kn.nlp.analysis.DL4JWord2Vec;
import de.fernuni_hagen.kn.nlp.analysis.DirectedHITS;
import de.fernuni_hagen.kn.nlp.analysis.DocumentSimilarity;
import de.fernuni_hagen.kn.nlp.analysis.HITS;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.analysis.TermSimilarity;
import de.fernuni_hagen.kn.nlp.db.ClearDatabase;
import de.fernuni_hagen.kn.nlp.preprocessing.FilePreprocessor;
import de.fernuni_hagen.kn.nlp.preprocessing.Preprocessor;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The use cases of this application.
 *
 * @author Nils Wende
 */
enum UseCases {
	CLEAR_DATABASE(ClearDatabase.class),
	PREPROCESSING(Preprocessor.class),
	FILE_PREPROCESSING(FilePreprocessor.class),
	BOOLEAN_RETRIEVAL(BooleanRetrieval.class),
	CENTROID_BY_MIN_AVG_DISTANCE(CentroidByMinAvgDistance.class),
	CENTROID_BY_SPREADING_ACTIVATION(CentroidBySpreadingActivation.class),
	DOCUMENT_SIMILARITY(DocumentSimilarity.class),
	HITS(HITS.class),
	DIRECTED_HITS(DirectedHITS.class),
	PAGE_RANK(PageRank.class),
	TERM_SIMILARITY(TermSimilarity.class),
	WORD2VEC(DL4JWord2Vec.class);

	private final Class<? extends UseCase> useCaseClass;

	UseCases(final Class<? extends UseCase> useCaseClass) {
		this.useCaseClass = useCaseClass;
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

	public Class<? extends UseCase> getUseCaseClass() {
		return useCaseClass;
	}

}

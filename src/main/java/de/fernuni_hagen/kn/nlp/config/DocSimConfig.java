package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.math.DocSimilarityFunction;

import java.util.List;

/**
 * Contains the document similarity config.
 *
 * @author Nils Wende
 */
public class DocSimConfig extends UseCaseConfig {

	private boolean calculate;
	private boolean useInverseDocFrequency;
	private double weightThreshold;
	private DocSimilarityFunction similarityFunction;
	private List<String> documents;

	public boolean calculate() {
		return calculate;
	}

	public boolean useInverseDocFrequency() {
		return useInverseDocFrequency;
	}

	public double getWeightThreshold() {
		return weightThreshold;
	}

	public DocSimilarityFunction getSimilarityFunction() {
		return similarityFunction == null ? DocSimilarityFunction.COSINE : similarityFunction;
	}

	public List<String> getDocuments() {
		return documents;
	}
}

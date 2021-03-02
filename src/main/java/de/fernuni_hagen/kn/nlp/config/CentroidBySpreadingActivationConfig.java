package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.math.WeightingFunction;

import java.util.List;

/**
 * Contains the centroid by spreading activation config.
 *
 * @author Nils Wende
 */
public class CentroidBySpreadingActivationConfig extends UseCaseConfig {

	private WeightingFunction weightingFunction;
	private List<String> query;

	public WeightingFunction getWeightingFunction() {
		return weightingFunction == null ? WeightingFunction.DICE : weightingFunction;
	}

	public List<String> getQuery() {
		return query;
	}
}

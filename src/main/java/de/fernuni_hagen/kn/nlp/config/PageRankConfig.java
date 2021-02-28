package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.math.WeightingFunction;

/**
 * Contains the PageRank config.
 *
 * @author Nils Wende
 */
public class PageRankConfig extends UseCaseConfig {

	private boolean calculate;
	private int iterations;
	private int resultLimit;
	private double weight;
	private WeightingFunction weightingFunction;

	public boolean calculate() {
		return calculate;
	}

	public int getIterations() {
		return iterations == 0 ? 25 : iterations;
	}

	public int getResultLimit() {
		return resultLimit == 0 ? Integer.MAX_VALUE : resultLimit;
	}

	public double getWeight() {
		return weight == 0 ? 0.85 : weight;
	}

	public WeightingFunction getWeightingFunction() {
		return weightingFunction == null ? WeightingFunction.DICE : weightingFunction;
	}
}

package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.math.WeightingFunction;

/**
 * Contains the HITS config.
 *
 * @author Nils Wende
 */
public class HITSConfig extends UseCaseConfig {

	private boolean calculate;
	private boolean directed;
	private int iterations;
	private int resultLimit;
	private WeightingFunction weightingFunction;

	public boolean calculate() {
		return calculate;
	}

	public boolean directed() {
		return directed;
	}

	public int getIterations() {
		return iterations == 0 ? 50 : iterations;
	}

	public int getResultLimit() {
		return resultLimit == 0 ? Integer.MAX_VALUE : resultLimit;
	}

	public WeightingFunction getWeightingFunction() {
		return weightingFunction == null ? WeightingFunction.DICE : weightingFunction;
	}
}

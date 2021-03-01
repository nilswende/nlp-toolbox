package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.math.WeightingFunction;

/**
 * Contains the term similarity config.
 *
 * @author Nils Wende
 */
public class TermSimConfig extends UseCaseConfig {

	private int compareFirstN;
	private WeightingFunction weightingFunction;
	private String term1;
	private String term2;

	public int getCompareFirstN() {
		return compareFirstN;
	}

	public WeightingFunction getWeightingFunction() {
		return weightingFunction == null ? WeightingFunction.DICE : weightingFunction;
	}

	public String getTerm1() {
		return term1;
	}

	public String getTerm2() {
		return term2;
	}
}

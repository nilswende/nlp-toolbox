package de.fernuni_hagen.kn.nlp.math;

/**
 * A weighting function.
 *
 * @author Nils Wende
 */
@FunctionalInterface
public interface WeightingFunction {

	/**
	 * Calculates the significance of {@code kij} in regard to {@code ki}, {@code kj} and {@code k}.
	 *
	 * @param ki  number of sentences that contain the term ti
	 * @param kj  number of sentences that contain the term tj
	 * @param kij number of sentences that contain both the term ti and tj
	 * @param k   total number of sentences
	 * @return the significance coefficient of kij
	 */
	double calculate(int ki, int kj, int kij, int k);

}

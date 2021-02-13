package de.fernuni_hagen.kn.nlp.math;

/**
 * Weighting functions for directed graphs.
 *
 * @author Nils Wende
 */
public enum DirectedWeightingFunction {
	/**
	 * Directed relationships.
	 */
	DIRECTED {
		@Override
		public double calculate(final long kij, final long kmax) {
			return kij / (double) kmax;
		}
	},
	/**
	 * Weighs everything equally.
	 */
	NONE {
		@Override
		public double calculate(final long kij, final long kmax) {
			return 1.0;
		}
	};

	/**
	 * Calculates the significance of {@code kij} in regard to {@code kmax}.
	 *
	 * @param kij  number of sentences that contain both the term ti and tj
	 * @param kmax maximum number of sentences that contain any term
	 * @return the significance coefficient of kij
	 */
	public abstract double calculate(long kij, long kmax);

}

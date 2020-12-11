package de.fernuni_hagen.kn.nlp.math;

/**
 * Concrete weighting functions.
 *
 * @author Nils Wende
 */
public enum WeightingFunctions implements WeightingFunction {
	/**
	 * Dice coefficient.
	 */
	DICE {
		@Override
		public double calculate(final int ki, final int kj, final int kij, final int k) {
			return (2 * kij) / (double) (ki + kj);
		}
	},
	/**
	 * Log-likelihood.
	 */
	LOG_LIKELIHOOD {
		@Override
		public double calculate(final int ki, final int kj, final int kij, final int k) {
			return nLogN(k) - nLogN(ki) - nLogN(kj) + nLogN(kij)
					+ nLogN(k - ki - kj + kij)
					+ nLogN(ki - kij) + nLogN(kj - kij)
					- nLogN(k - ki) - nLogN(k - kj);
		}

		private double nLogN(final double n) {
			return n <= 0 ? 0 : n * Math.log(n);
		}
	}
}

package de.fernuni_hagen.kn.nlp.math;

import static org.apache.commons.math3.util.CombinatoricsUtils.factorialLog;

/**
 * Concrete weighting functions.
 *
 * @author Nils Wende
 */
public enum WeightingFunctions implements WeightingFunction {
	/**
	 * Jaccard index.
	 */
	JACCARD {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k) {
			return (kij / (double) (ki + kj - kij));
		}
	},
	/**
	 * Dice coefficient.
	 */
	DICE {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k) {
			return 2 * (kij / (double) (ki + kj));
		}
	},
	/**
	 * Mutual information.
	 */
	MUTUAL_INFORMATION {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k) {
			return Math.log((k * kij) / (double) (ki * kj));
		}
	},
	/**
	 * Log-likelihood.
	 */
	LOG_LIKELIHOOD {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k) {
			return nLogN(k) - nLogN(ki) - nLogN(kj) + nLogN(kij)
					+ nLogN(k - ki - kj + kij)
					+ nLogN(ki - kij) + nLogN(kj - kij)
					- nLogN(k - ki) - nLogN(k - kj);
		}

		private double nLogN(final double n) {
			return n <= 0 ? 0 : n * Math.log(n);
		}
	},
	/**
	 * Poisson distribution.
	 */
	POISSON {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k) {
			final var div = (ki * kj) / (double) k;
			return (factorialLog((int) kij) - kij * Math.log(div) + div) / Math.log(k);
		}
	},
	/**
	 * Weighs everything equally.
	 */
	NONE {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k) {
			return 1.0;
		}
	}
}

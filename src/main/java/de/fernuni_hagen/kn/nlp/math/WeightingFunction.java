package de.fernuni_hagen.kn.nlp.math;

/**
 * Weighting functions for graph relationships.
 *
 * @author Nils Wende
 */
public enum WeightingFunction {
	/**
	 * Jaccard index.
	 */
	JACCARD {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k, final long kmax) {
			return (kij / (double) (ki + kj - kij));
		}
	},
	/**
	 * Dice coefficient.
	 */
	DICE {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k, final long kmax) {
			return 2 * (kij / (double) (ki + kj));
		}
	},
	/**
	 * Mutual information.
	 */
	MUTUAL_INFORMATION {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k, final long kmax) {
			return Math.log10((k * kij) / (double) (ki * kj));
		}
	},
	/**
	 * Log-likelihood.
	 */
	LOG_LIKELIHOOD {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k, final long kmax) {
			return nLogN(k) - nLogN(ki) - nLogN(kj) + nLogN(kij)
					+ nLogN(k - ki - kj + kij)
					+ nLogN(ki - kij) + nLogN(kj - kij)
					- nLogN(k - ki) - nLogN(k - kj);
		}

		private double nLogN(final double n) {
			return n <= 0 ? 0 : n * Math.log10(n);
		}
	},
	/**
	 * Poisson distribution.
	 */
	POISSON {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k, final long kmax) {
			final var div = (ki * kj) / (double) k;
			return (factorialLog10((int) kij) - kij * Math.log10(div) + div) / Math.log10(k);
		}
	},
	/**
	 * Association.
	 */
	ASSN {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k, final long kmax) {
			return kij / (double) kmax;
		}
	},
	/**
	 * Weighs everything equally.
	 */
	NONE {
		@Override
		public double calculate(final long ki, final long kj, final long kij, final long k, final long kmax) {
			return 1.0;
		}
	};

	/**
	 * Calculates the significance of the terms ti and tj.
	 *
	 * @param ki   number of sentences that contain the term ti
	 * @param kj   number of sentences that contain the term tj
	 * @param kij  number of sentences that contain both the term ti and tj
	 * @param k    total number of sentences
	 * @param kmax maximum number of sentences that contain any term
	 * @return the significance coefficient
	 */
	public abstract double calculate(long ki, long kj, long kij, long k, long kmax);

	private static double factorialLog10(final int n) {
		double logSum = 0;
		for (int i = 2; i <= n; i++) {
			logSum += Math.log10(i);
		}
		return logSum;
	}

}

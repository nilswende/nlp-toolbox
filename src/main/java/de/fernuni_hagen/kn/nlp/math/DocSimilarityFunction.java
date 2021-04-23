package de.fernuni_hagen.kn.nlp.math;

import org.apache.commons.collections4.SetUtils;

import java.util.Map;
import java.util.Set;

/**
 * Document similarity functions.
 *
 * @author Nils Wende
 */
public enum DocSimilarityFunction {
	DICE {
		@Override
		public double calculate(final Map<String, Double> d1, final Map<String, Double> d2) {
			return 2 * getCommonTerms(d1, d2).size() / (double) (d1.size() + d2.size());
		}
	},
	EUCLID {
		@Override
		public double calculate(final Map<String, Double> d1, final Map<String, Double> d2) {
			final var sum = getAllTerms(d1, d2).stream()
					.mapToDouble(k -> d1.getOrDefault(k, 0.0) + d2.getOrDefault(k, 0.0))
					.map(w -> w * w)
					.sum();
			return Math.sqrt(sum);
		}
	},
	COSINE {
		@Override
		public double calculate(final Map<String, Double> d1, final Map<String, Double> d2) {
			final var dividend = getAllTerms(d1, d2).stream()
					.mapToDouble(k -> d1.getOrDefault(k, 0.0) * d2.getOrDefault(k, 0.0))
					.sum();
			final var divisor1 = sumWeight(d1);
			final var divisor2 = sumWeight(d2);
			return dividend / (divisor1 * divisor2);
		}
	};

	private static Set<String> getCommonTerms(Map<String, Double> d1, Map<String, Double> d2) {
		return SetUtils.intersection(d1.keySet(), d2.keySet());
	}

	private static Set<String> getAllTerms(final Map<String, Double> d1, final Map<String, Double> d2) {
		return SetUtils.union(d1.keySet(), d2.keySet());
	}

	private static double sumWeight(final Map<String, Double> docVec) {
		final var sum = docVec.values().stream()
				.mapToDouble(d -> d * d)
				.sum();
		return Math.sqrt(sum);
	}

	/**
	 * Calculates the similarity of the two document vectors.
	 *
	 * @param d1 document vector containing the term weights of document one
	 * @param d2 document vector containing the term weights of document two
	 * @return the similarity of the two documents
	 */
	public abstract double calculate(Map<String, Double> d1, Map<String, Double> d2);

	/**
	 * Converts a similarity to a distance.
	 *
	 * @param similarity similarity != 0
	 * @return distance
	 */
	public static double toDistance(final double similarity) {
		return (1 / similarity) - 1;
	}

}

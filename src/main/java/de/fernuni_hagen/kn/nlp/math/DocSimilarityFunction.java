package de.fernuni_hagen.kn.nlp.math;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Document similarity functions.
 *
 * @author Nils Wende
 */
public enum DocSimilarityFunction {
	EUCLID {
		@Override
		public double calculate(final Map<String, Double> d1, final Map<String, Double> d2) {
			final var sum = getAllKeys(d1, d2).stream()
					.mapToDouble(k -> d1.getOrDefault(k, 0.0) + d2.getOrDefault(k, 0.0))
					.map(w -> w * w)
					.sum();
			return Math.sqrt(sum);
		}
	},
	COSINE {
		@Override
		public double calculate(final Map<String, Double> d1, final Map<String, Double> d2) {
			final var dividend = getAllKeys(d1, d2).stream()
					.mapToDouble(k -> d1.getOrDefault(k, 0.0) * d2.getOrDefault(k, 0.0))
					.sum();
			final var divisor1 = sumWeight(d1);
			final var divisor2 = sumWeight(d2);
			return dividend / (divisor1 * divisor2);
		}
	};

	private static Set<String> getAllKeys(final Map<String, Double> d1, final Map<String, Double> d2) {
		final var keys = new HashSet<>(d1.keySet());
		keys.addAll(d2.keySet());
		return keys;
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

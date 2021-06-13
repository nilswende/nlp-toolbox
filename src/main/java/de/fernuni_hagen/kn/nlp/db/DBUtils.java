package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.math.WeightingFunction;

import java.util.Map;
import java.util.TreeMap;

/**
 * Database utilities.
 *
 * @author Nils Wende
 */
public final class DBUtils {

	private DBUtils() {
		throw new AssertionError("no init");
	}

	/**
	 * Gets the significance coefficient of the term cooccurrences via the weighting function.
	 *
	 * @param map      map containing the cooccurrences
	 * @param ti       first term
	 * @param tj       second term
	 * @param ki       number of sentences that contain the term ti
	 * @param kj       number of sentences that contain the term tj
	 * @param kij      number of sentences that contain both the term ti and tj
	 * @param k        total number of sentences
	 * @param kmax     maximum number of sentences that contain any term
	 * @param function the weighting function
	 */
	public static void putSignificance(final Map<String, Map<String, Double>> map, final String ti, final String tj, final long ki, final long kj, final Long kij, final long k, final long kmax, final WeightingFunction function) {
		var sig = 0.01;
		if (ki > 1 || kj > 1) {
			sig = function.calculate(ki, kj, kij, k, kmax);
		}
		map.computeIfAbsent(ti, x -> new TreeMap<>()).put(tj, sig);
	}

	/**
	 * Gets the significance coefficient of the directed term cooccurrences via the weighting function.
	 *
	 * @param map      map containing the cooccurrences
	 * @param ti       first term
	 * @param tj       second term
	 * @param ki       number of sentences that contain the term ti
	 * @param kj       number of sentences that contain the term tj
	 * @param kij      number of sentences that contain both the term ti and tj
	 * @param k        total number of sentences
	 * @param kmax     maximum number of sentences that contain any term
	 * @param function the weighting function
	 */
	public static void putDirectedSignificance(final Map<String, Map<String, Double>> map, final String ti, final String tj, final long ki, final long kj, final Long kij, final long k, final long kmax, final WeightingFunction function) {
		var sig = 0.01;
		if ((ki > 1 || kj > 1) && kj >= ki) {
			final var minKij = Math.min(kij, Math.min(ki, kj));
			sig = function.calculate(ki, kj, minKij, k, kmax);
		}
		map.computeIfAbsent(ti, x -> new TreeMap<>()).put(tj, sig);
	}

}

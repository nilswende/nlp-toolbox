package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Calculates the PageRanks for all terms in the DB.
 *
 * @author Nils Wende
 */
public class PageRank {

	private static final double WEIGHT = 0.85;
	private static final double INV_WEIGHT = 1 - WEIGHT;
	private static final int ITERATIONS = 25;

	/**
	 * Calculates the PageRanks for all terms in the DB.
	 *
	 * @param db       DB
	 * @param function WeightingFunction
	 * @return PageRanks
	 */
	public Map<String, Double> calculate(final DBReader db, final WeightingFunction function) {
		final var significances = db.getSignificances(function);

		final var pageRanks = initPageRanks(significances.keySet());
		for (int i = 0; i < ITERATIONS; i++) {
			calculate(pageRanks, significances);
		}
		return normalize(pageRanks);
	}

	private Map<String, Double> initPageRanks(final Set<String> terms) {
		final var pageRanks = new TreeMap<String, Double>();
		final Double init = INV_WEIGHT;
		terms.forEach(t -> pageRanks.put(t, init));
		return pageRanks;
	}

	private void calculate(final Map<String, Double> pageRanks, final Map<String, Map<String, Double>> significances) {
		significances.forEach((t1, v) -> {
			final double pr = INV_WEIGHT + WEIGHT * sumAdjacentPageRanks(pageRanks, v);
			pageRanks.put(t1, pr);
		});
	}

	private double sumAdjacentPageRanks(final Map<String, Double> pageRanks, final Map<String, Double> v) {
		return v.entrySet().stream()
				.mapToDouble(e -> (pageRanks.get(e.getKey()) * e.getValue()) / v.size())
				.sum();
	}

	private Map<String, Double> normalize(final Map<String, Double> pageRanks) {
		pageRanks.values().stream()
				.max(Double::compareTo)
				.ifPresent(maxPageRank -> normalize(pageRanks, maxPageRank));
		return pageRanks;
	}

	private void normalize(final Map<String, Double> pageRanks, final double maxPageRank) {
		pageRanks.replaceAll((t, pr) -> pr / maxPageRank);
	}

}

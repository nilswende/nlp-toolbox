package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.config.UseCaseConfig;
import de.fernuni_hagen.kn.nlp.graph.BreadthFirstGraphSearcher;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calculates the PageRanks for all terms in the DB.
 *
 * @author Nils Wende
 */
public class PageRank extends UseCase {

	private final Config config;
	private final double weight;
	private final double invWeight;

	public PageRank(final Config config) {
		this.config = config;
		weight = config.getWeight();
		invWeight = 1 - weight;
	}

	/**
	 * PageRank config.
	 */
	public static class Config extends UseCaseConfig {
		private boolean calculate;
		private int iterations;
		private int resultLimit;
		private double weight;
		private WeightingFunction weightingFunction;

		public boolean calculate() {
			return calculate;
		}

		public int getIterations() {
			return iterations == 0 ? 25 : iterations;
		}

		public int getResultLimit() {
			return resultLimit == 0 ? Integer.MAX_VALUE : resultLimit;
		}

		public double getWeight() {
			return weight == 0 ? 0.85 : weight;
		}

		public WeightingFunction getWeightingFunction() {
			return weightingFunction == null ? WeightingFunction.DICE : weightingFunction;
		}
	}

	@Override
	public void execute(final DBReader dbReader) {
		final var pageRanks = calculate(dbReader);
		printNameAnd(pageRanks);
	}

	/**
	 * Calculates the PageRanks for all terms in the DB.
	 *
	 * @param db DB
	 * @return PageRanks
	 */
	public Map<String, Double> calculate(final DBReader db) {
		final var significances = db.getSignificances(config.getWeightingFunction());
		new BreadthFirstGraphSearcher().findBiggestSubgraph(significances);

		final var pageRanks = initPageRanks(significances.keySet());
		for (int i = 0; i < config.getIterations(); i++) {
			calculate(pageRanks, significances);
		}
		return normalize(pageRanks);
	}

	private Map<String, Double> initPageRanks(final Set<String> terms) {
		return terms.stream().collect(Collectors.toMap(t -> t, t -> invWeight));
	}

	private void calculate(final Map<String, Double> pageRanks, final Map<String, Map<String, Double>> significances) {
		significances.forEach((t1, adjacent) -> {
			final double pr = invWeight + weight * sumAdjacentPageRanks(pageRanks, adjacent, significances);
			pageRanks.put(t1, pr);
		});
	}

	private double sumAdjacentPageRanks(final Map<String, Double> pageRanks, final Map<String, Double> adjacent, final Map<String, Map<String, Double>> significances) {
		return adjacent.entrySet().stream()
				.mapToDouble(e -> (pageRanks.get(e.getKey()) * e.getValue()) / significances.get(e.getKey()).size())
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

package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.graph.BreadthFirstGraphSearcher;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calculates the PageRanks for all terms in the DB.
 *
 * @author Nils Wende
 */
public class PageRank extends UseCase {

	private int iterations = 25;
	private int resultLimit;
	private double weight = 0.85;
	private WeightingFunction weightingFunction = WeightingFunction.DICE;

	private Result result;

	/**
	 * PageRank result.
	 */
	public static class Result extends UseCase.Result {
		private final Map<String, Double> scores;

		Result(final Map<String, Double> scores, final int resultLimit) {
			this.scores = resultLimit == 0 ? scores : Maps.topN(scores, resultLimit);
		}

		@Override
		protected void printResult() {
			this.scores.forEach((term, score) -> printf("PageRank of %s: %s", term, score));
		}

		/**
		 * Returns the calculated PageRank scores.
		 *
		 * @return the calculated PageRank scores
		 */
		public Map<String, Double> getScores() {
			return scores;
		}
	}

	@Override
	public void execute(final DBReader dbReader) {
		final var significances = dbReader.getSignificances(weightingFunction);
		new BreadthFirstGraphSearcher().findBiggestSubgraph(significances);

		final var pageRanks = initPageRanks(significances.keySet());
		for (int i = 0; i < iterations; i++) {
			calculate(pageRanks, significances);
		}
		final var normalizedPageRanks = normalize(pageRanks);
		result = new Result(normalizedPageRanks, resultLimit);
	}

	private Map<String, Double> initPageRanks(final Set<String> terms) {
		return terms.stream().collect(Collectors.toMap(t -> t, t -> 1 - weight));
	}

	private void calculate(final Map<String, Double> pageRanks, final Map<String, Map<String, Double>> significances) {
		significances.forEach((t1, adjacent) -> {
			final double pr = 1 - weight + weight * sumAdjacentPageRanks(pageRanks, adjacent, significances);
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
				.max(Comparator.naturalOrder())
				.ifPresent(maxPageRank -> normalize(pageRanks, maxPageRank));
		return pageRanks;
	}

	private void normalize(final Map<String, Double> pageRanks, final double maxPageRank) {
		pageRanks.replaceAll((t, pr) -> pr / maxPageRank);
	}

	@Override
	public Result getResult() {
		return result;
	}

	/**
	 * Set the number of times the algorithm should be executed before returning.
	 *
	 * @param iterations the number of iterations
	 * @return this object
	 */
	public PageRank setIterations(final int iterations) {
		this.iterations = iterations;
		return this;
	}

	/**
	 * Set the number of terms with the highest score that should be returned.
	 *
	 * @param resultLimit the number of terms
	 * @return this object
	 */
	public PageRank setResultLimit(final int resultLimit) {
		this.resultLimit = resultLimit;
		return this;
	}

	/**
	 * Set the weighting factor used in spreading the score from node to node.
	 *
	 * @param weight the weighting factor
	 * @return this object
	 */
	public PageRank setWeight(final double weight) {
		this.weight = weight;
		return this;
	}

	/**
	 * Set the function to calculate the weight of each cooccurrence.
	 *
	 * @param weightingFunction the weighting function
	 * @return this object
	 */
	public PageRank setWeightingFunction(final WeightingFunction weightingFunction) {
		this.weightingFunction = weightingFunction;
		return this;
	}
}

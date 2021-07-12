package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.file.FileSaver;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calculates the PageRank scores of a graph.
 *
 * @author Nils Wende
 */
public class PageRank extends UseCase {

	private final FileSaver fileSaver = new FileSaver("data/output/PageRank.txt", false);

	private int iterations = 25;
	private int resultLimit = Integer.MAX_VALUE;
	private double weight = 0.85;
	private WeightingFunction weightingFunction = WeightingFunction.ASSN;

	private transient Result result;

	/**
	 * PageRank result.
	 */
	public static class Result extends UseCase.Result {
		private final Map<String, Double> scores;

		Result(final Map<String, Double> scores, final int resultLimit) {
			this.scores = Maps.topN(scores, resultLimit);
		}

		@Override
		protected void printResult() {
			print("PageRank scores:");
			printMap(scores);
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
		final var significances = dbReader.getDirectedSignificances(weightingFunction);

		final var pageRanks = initPageRanks(significances.keySet());
		for (int i = 0; i < iterations; i++) {
			calculate(pageRanks, significances);
		}
		final var normalizedPageRanks = normalize(pageRanks);
		result = new Result(normalizedPageRanks, resultLimit);
		fileSaver.print(result);
	}

	private Map<String, Double> initPageRanks(final Set<String> terms) {
		return terms.stream().collect(Collectors.toMap(t -> t, t -> 1 - weight));
	}

	private void calculate(final Map<String, Double> pageRanks, final Map<String, Map<String, Double>> significances) {
		significances.forEach((ti, adjacent) -> {
			final var pr = 1 - weight + weight * sumAdjacentPageRanks(ti, pageRanks, adjacent.keySet(), significances);
			pageRanks.put(ti, pr);
		});
	}

	private double sumAdjacentPageRanks(final String ti,
										final Map<String, Double> pageRanks,
										final Set<String> adjacent,
										final Map<String, Map<String, Double>> significances) {
		return adjacent.stream()
				.mapToDouble(tj -> (pageRanks.get(tj) * significances.get(tj).get(ti)) / significances.get(tj).size())
				.sum();
	}

	private Map<String, Double> normalize(final Map<String, Double> pageRanks) {
		pageRanks.values().stream()
				.max(Comparator.naturalOrder())
				.ifPresent(maxPageRank -> Maps.normalize(pageRanks, maxPageRank));
		return pageRanks;
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

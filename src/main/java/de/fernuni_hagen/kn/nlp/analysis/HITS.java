package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Uses the HITS algorithm to find hubs and authorities in a graph.
 *
 * @author Nils Wende
 */
public class HITS extends UseCase {

	int iterations = 50;
	int resultLimit = Integer.MAX_VALUE;
	WeightingFunction weightingFunction = WeightingFunction.ASSN;

	Result result;

	/**
	 * HITS result.
	 */
	public static class Result extends UseCase.Result {
		private final Set<String> terms;
		private final Map<String, Double> authorityScores;
		private final Map<String, Double> hubScores;

		Result(final Set<String> terms, final Map<String, Double> auths, final Map<String, Double> hubs, final int resultLimit) {
			this.terms = terms;
			this.authorityScores = Maps.topN(auths, resultLimit);
			this.hubScores = Maps.topN(hubs, resultLimit);
		}

		@Override
		protected void printResult() {
			authorityScores.forEach((term, score) -> printf("Authority score of %s: %s", term, score));
			hubScores.forEach((term, score) -> printf("Hub score of %s: %s", term, score));
		}

		/**
		 * Returns the set of all terms in the graph.
		 *
		 * @return the set of all terms
		 */
		public Set<String> getTerms() {
			return terms;
		}

		/**
		 * Returns the calculated authority scores.
		 *
		 * @return the calculated authority scores
		 */
		public Map<String, Double> getAuthorityScores() {
			return authorityScores;
		}

		/**
		 * Returns the calculated hub scores.
		 *
		 * @return the calculated hub scores
		 */
		public Map<String, Double> getHubScores() {
			return hubScores;
		}
	}

	@Override
	public void execute(final DBReader dbReader) {
		final Map<String, Map<String, Double>> graph = dbReader.getSignificances(weightingFunction);
		calcScores(graph, graph);
	}

	/**
	 * Calculates the scores for each direction.
	 *
	 * @param auth2hubs traversal direction from authority nodes to hub nodes
	 * @param hub2auths traversal direction from hub nodes to authority nodes
	 */
	protected void calcScores(final Map<String, Map<String, Double>> auth2hubs, final Map<String, Map<String, Double>> hub2auths) {
		final Set<String> terms = getTerms(auth2hubs);
		final Map<String, Double> auths = initMap(terms);
		final Map<String, Double> hubs = initMap(terms);
		for (int i = 0; i < iterations; i++) {
			calcScore(auths, auth2hubs, hubs);
			calcScore(hubs, hub2auths, auths);
		}
		result = new Result(terms, auths, hubs, resultLimit);
	}

	/**
	 * Returns the set of all terms in the graph.
	 *
	 * @param graph the graph whose distinct nodes will be extracted
	 * @return the set of all terms
	 */
	protected Set<String> getTerms(final Map<String, Map<String, Double>> graph) {
		return graph.keySet();
	}

	private Map<String, Double> initMap(final Set<String> terms) {
		return terms.stream().collect(Collectors.toMap(t -> t, t -> 1.0));
	}

	private void calcScore(final Map<String, Double> targetScore, final Map<String, Map<String, Double>> linking, final Map<String, Double> otherScore) {
		double tempNorm = 0;
		for (final Map.Entry<String, Double> entry : targetScore.entrySet()) {
			final var node = entry.getKey();
			final var linked = linking.getOrDefault(node, Map.of());
			final double sum = sumOtherScore(linked, otherScore);
			entry.setValue(sum);
			tempNorm += sum * sum;
		}
		if (tempNorm != 0) {
			final double norm = Math.sqrt(tempNorm);
			targetScore.replaceAll((t, s) -> s / norm);
		}
	}

	private double sumOtherScore(final Map<String, Double> linked, final Map<String, Double> otherScore) {
		return linked.entrySet().stream()
				.mapToDouble(e -> otherScore.get(e.getKey()) * e.getValue())
				.sum();
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
	public HITS setIterations(final int iterations) {
		this.iterations = iterations;
		return this;
	}

	/**
	 * Set the number of terms with the highest score that should be returned.
	 *
	 * @param resultLimit the number of terms
	 * @return this object
	 */
	public HITS setResultLimit(final int resultLimit) {
		this.resultLimit = resultLimit;
		return this;
	}

	/**
	 * Set the function to calculate the weight of each cooccurrence.
	 *
	 * @param weightingFunction the weighting function
	 * @return this object
	 */
	public HITS setWeightingFunction(final WeightingFunction weightingFunction) {
		this.weightingFunction = weightingFunction;
		return this;
	}
}

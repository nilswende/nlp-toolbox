package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Uses the HITS algorithm to find hubs and authorities in a graph.
 *
 * @author Nils Wende
 */
public class HITS extends UseCase {

	private int iterations = 50;
	private int resultLimit = Integer.MAX_VALUE;
	private WeightingFunction weightingFunction = WeightingFunction.ASSN;

	private transient Result result;

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
			printfMap(authorityScores, "No authority scores", "Authority score of %s: %s");
			printfMap(hubScores, "No hub scores", "Hub score of %s: %s");
		}

		/**
		 * Returns the set of all terms associated with a score.
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
		final var significances = dbReader.getDirectedSignificances(weightingFunction);
		final var terms = significances.keySet();
		final var auths = initMap(terms);
		final var hubs = initMap(terms);
		for (int i = 0; i < iterations; i++) {
			calcScore(auths, significances, hubs, (ti, tj) -> significances.get(tj).get(ti));
			calcScore(hubs, significances, auths, (ti, tj) -> significances.get(ti).get(tj));
		}
		result = new Result(terms, auths, hubs, resultLimit);
	}

	private Map<String, Double> initMap(final Set<String> terms) {
		return terms.stream().collect(Collectors.toMap(t -> t, t -> .15));
	}

	private void calcScore(final Map<String, Double> targetScore,
						   final Map<String, Map<String, Double>> linking,
						   final Map<String, Double> otherScore,
						   final BiFunction<String, String, Double> weighting) {
		for (final var entry : targetScore.entrySet()) {
			final var ti = entry.getKey();
			final var adjacent = linking.get(ti);
			final var sum = sumOtherScore(ti, adjacent.keySet(), otherScore, weighting);
			entry.setValue(sum);
		}
		normalize(targetScore);
	}

	private double sumOtherScore(final String ti,
								 final Set<String> adjacent,
								 final Map<String, Double> otherScore,
								 final BiFunction<String, String, Double> weighting) {
		return adjacent.stream()
				.mapToDouble(tj -> otherScore.get(tj) * weighting.apply(ti, tj))
				.sum();
	}

	private void normalize(final Map<String, Double> scores) {
		final var norm = scores.values().stream()
				.mapToDouble(d -> d * d)
				.sum();
		if (norm != 0) {
			Maps.normalize(scores, Math.sqrt(norm));
		}
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

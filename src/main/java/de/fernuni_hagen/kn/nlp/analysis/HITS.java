package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Uses the HITS algorithm to find hubs and authorities in a graph.
 *
 * @author Nils Wende
 */
public class HITS {

	private static final int ITERATIONS = 50;

	/**
	 * Uses the HITS algorithm to find hubs and authorities in a graph.
	 *
	 * @param db DB
	 * @return HITS scores
	 */
	public Map<String, Scores> calculate(final DBReader db) {
		final Map<String, List<String>> cooccurrences = db.getCooccurrences();
		final var terms = cooccurrences.keySet();
		final Map<String, Double> auths = initMap(terms);
		final Map<String, Double> hubs = initMap(terms);
		for (int i = 0; i < ITERATIONS; i++) {
			calcScore(auths, hubs, cooccurrences);
			calcScore(hubs, auths, cooccurrences);
		}
		return createResultMap(terms, auths, hubs);
	}

	private Map<String, Double> initMap(final Set<String> terms) {
		final var map = new TreeMap<String, Double>();
		final Double init = 1.0;
		terms.forEach(t -> map.put(t, init));
		return map;
	}

	private void calcScore(final Map<String, Double> targetScore, final Map<String, Double> otherScore, final Map<String, List<String>> cooccurrences) {
		double tempNorm = 0;
		for (final Map.Entry<String, List<String>> entry : cooccurrences.entrySet()) {
			final String t = entry.getKey();
			final List<String> cooccs = entry.getValue();
			final var sum = cooccs.stream().mapToDouble(otherScore::get).sum();
			targetScore.put(t, sum);
			tempNorm += sum * sum;
		}
		final double norm = Math.sqrt(tempNorm);
		targetScore.replaceAll((t, s) -> s / norm);
	}

	private Map<String, Scores> createResultMap(final Set<String> terms, final Map<String, Double> auths, final Map<String, Double> hubs) {
		final var map = new TreeMap<String, Scores>();
		for (final String term : terms) {
			final var scores = new Scores(auths.get(term), hubs.get(term));
			map.put(term, scores);
		}
		return map;
	}

	/**
	 * DTO for the HITS scores.
	 */
	public static class Scores {

		private final double authorityScore, hubScore;

		public Scores(final double authorityScore, final double hubScore) {
			this.authorityScore = authorityScore;
			this.hubScore = hubScore;
		}

		public double getAuthorityScore() {
			return authorityScore;
		}

		public double getHubScore() {
			return hubScore;
		}

	}

}

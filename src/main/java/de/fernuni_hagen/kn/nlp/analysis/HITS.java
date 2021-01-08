package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.HITSConfig;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Uses the HITS algorithm to find hubs and authorities in a graph.
 *
 * @author Nils Wende
 */
public class HITS {

	private final HITSConfig hitsConfig;

	public HITS(final HITSConfig hitsConfig) {
		this.hitsConfig = hitsConfig;
	}

	/**
	 * Uses the HITS algorithm to find hubs and authorities in a graph.
	 *
	 * @param db DB
	 * @return HITS scores
	 */
	public Map<String, Scores> calculate(final DBReader db) {
		final Map<String, Map<String, Double>> significances = db.getSignificances(hitsConfig.getDirectedWeightingFunction());
		final var terms = significances.keySet();
		final Map<String, Double> auths = initMap(terms);
		final Map<String, Double> hubs = initMap(terms);
		for (int i = 0; i < hitsConfig.getIterations(); i++) {
			calcScore(auths, hubs, significances);
			calcScore(hubs, auths, significances);
		}
		return createResultMap(terms, auths, hubs);
	}

	private Map<String, Double> initMap(final Set<String> terms) {
		final var map = new TreeMap<String, Double>();
		final Double init = 1.0;
		terms.forEach(t -> map.put(t, init));
		return map;
	}

	private void calcScore(final Map<String, Double> targetScore, final Map<String, Double> otherScore, final Map<String, Map<String, Double>> significances) {
		double tempNorm = 0;
		for (final Map.Entry<String, Map<String, Double>> cooccs : significances.entrySet()) {
			double sum = 0;
			for (final Map.Entry<String, Double> sigs : cooccs.getValue().entrySet()) {
				sum += otherScore.get(sigs.getKey()) * sigs.getValue();
			}
			targetScore.put(cooccs.getKey(), sum);
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

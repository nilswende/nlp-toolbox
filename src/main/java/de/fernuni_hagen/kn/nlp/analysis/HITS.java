package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.HITSConfig;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Uses the HITS algorithm to find hubs and authorities in a graph.
 *
 * @author Nils Wende
 */
public class HITS {

	protected final HITSConfig hitsConfig;

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
		final Map<String, Map<String, Double>> linking = db.getSignificances(hitsConfig.getWeightingFunction());
		return getStringScoresMap(linking, linking);
	}

	protected Map<String, Scores> getStringScoresMap(final Map<String, Map<String, Double>> auth2hubs, final Map<String, Map<String, Double>> hub2auths) {
		final Set<String> terms = getTerms(auth2hubs);
		final Map<String, Double> auths = initMap(terms);
		final Map<String, Double> hubs = initMap(terms);
		for (int i = 0; i < hitsConfig.getIterations(); i++) {
			calcScore(auths, auth2hubs, hubs);
			calcScore(hubs, hub2auths, auths);
		}
		return createResultMap(terms, auths, hubs);
	}

	protected Set<String> getTerms(final Map<String, Map<String, Double>> linking) {
		return linking.keySet();
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

	private Map<String, Scores> createResultMap(final Set<String> terms, final Map<String, Double> auths, final Map<String, Double> hubs) {
		return terms.stream().collect(Collectors.toMap(t -> t, t -> new Scores(auths.get(t), hubs.get(t))));
	}

	/**
	 * Creates a new HITS instance from the given config.
	 *
	 * @param config HITSConfig
	 * @return a new HITS instance
	 */
	public static HITS from(final HITSConfig config) {
		return config.directed() ? new DirectedHITS(config) : new HITS(config);
	}

	/**
	 * DTO for the HITS scores.
	 */
	public static class Scores {
		private final double authorityScore, hubScore;

		Scores(final double authorityScore, final double hubScore) {
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

package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.config.UseCaseConfig;
import de.fernuni_hagen.kn.nlp.graph.DijkstraSearcher;
import de.fernuni_hagen.kn.nlp.graph.WeightedPath;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.List;
import java.util.Map;

/**
 * Finds a centroid by calculating the minimum average distance.
 *
 * @author Nils Wende
 */
public class CentroidByMinAvgDistance extends UseCase {

	private final Config config;

	CentroidByMinAvgDistance(final Config config) {
		this.config = config;
	}

	/**
	 * CentroidByMinAvgDistance config.
	 */
	public static class Config extends UseCaseConfig {
		private WeightingFunction weightingFunction;
		private List<String> query;

		public WeightingFunction getWeightingFunction() {
			return weightingFunction == null ? WeightingFunction.DICE : weightingFunction;
		}

		public List<String> getQuery() {
			return query;
		}
	}

	@Override
	public void execute(final DBReader dbReader) {
		final var centroid = calculate(dbReader);
		print(centroid);
	}

	/**
	 * Finds the centroid of the given query set (most frequent terms).
	 *
	 * @param db DB
	 * @return the centroid or null, if the query is too diverse
	 */
	public String calculate(final DBReader db) {
		final List<String> query = config.getQuery();
		if (query == null || query.size() <= 1) {
			return null;
		}
		final var significances = db.getSignificances(config.getWeightingFunction());

		final var distances = Maps.invertValues(significances);
		return findCentroid(query, distances);
	}

	private String findCentroid(final List<String> query, final Map<String, Map<String, Double>> distances) {
		String centroid = null;
		double minAvgDistance = Double.MAX_VALUE;
		final var dijkstra = new DijkstraSearcher();
		for (final String term : distances.keySet()) {
			if (!query.contains(term)) {
				final var avgDistance = query.stream()
						.map(q -> dijkstra.search(term, q, distances))
						.filter(WeightedPath::exists)
						.mapToDouble(WeightedPath::getWeight)
						.average()
						.orElse(Double.MAX_VALUE);
				if (avgDistance < minAvgDistance) {
					centroid = term;
					minAvgDistance = avgDistance;
				}
			}
		}
		return centroid;
	}

}

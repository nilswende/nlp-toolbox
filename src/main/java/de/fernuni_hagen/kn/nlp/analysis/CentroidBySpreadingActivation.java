package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.config.UseCaseConfig;
import de.fernuni_hagen.kn.nlp.graph.BreadthFirstGraphSearcher;
import de.fernuni_hagen.kn.nlp.graph.DijkstraSearcher;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Finds a centroid by spreading activation.
 *
 * @author Nils Wende
 */
public class CentroidBySpreadingActivation extends UseCase {

	private final Config config;

	CentroidBySpreadingActivation(final Config config) {
		this.config = config;
	}

	@Override
	public void execute(final DBReader dbReader) {
		final var centroid = calculate(dbReader);
		printNameAnd(centroid);
	}

	/**
	 * CentroidBySpreadingActivation config.
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

	/**
	 * Finds the centroid of the given query set (most frequent terms).
	 *
	 * @param db DB
	 * @return the centroid or null, if the query is too diverse
	 */
	public String calculate(final DBReader db) {
		final var significances = db.getSignificances(WeightingFunction.DICE);
		final var cleanedQuery = cleanQuery(config.getQuery(), significances);
		if (cleanedQuery == null) {
			return null;
		}

		significances.values().forEach(m -> m.replaceAll((k, v) -> 1 / v));
		return findCentroid(cleanedQuery, significances);
	}

	private List<String> cleanQuery(final List<String> query, final Map<String, Map<String, Double>> significances) {
		var cleanedQuery = query;
		if (cleanedQuery == null || cleanedQuery.size() <= 1) {
			return null;
		}
		cleanedQuery = getDbQuery(cleanedQuery, significances);
		if (cleanedQuery.size() <= 1) {
			return null;
		}
		cleanedQuery = getSubgraphQuery(cleanedQuery, significances);
		if (cleanedQuery.size() <= 1) {
			return null;
		}
		return cleanedQuery;
	}

	private List<String> getDbQuery(final List<String> query, final Map<String, Map<String, Double>> significances) {
		return query.stream().filter(significances::containsKey).collect(Collectors.toList());
	}

	private List<String> getSubgraphQuery(final List<String> query, final Map<String, Map<String, Double>> significances) {
		var subgraphQuery = List.<String>of();
		final var bfs = new BreadthFirstGraphSearcher();
		for (final var queryTerm : query) {
			final var connected = bfs.search(queryTerm, significances);
			final var connectedQuery = query.stream().filter(connected::contains).collect(Collectors.toList());
			if (connectedQuery.size() > subgraphQuery.size()) {
				subgraphQuery = connectedQuery;
			}
			if (subgraphQuery.size() == query.size()) {
				break;
			}
		}
		return subgraphQuery;
	}

	// a centroid will be found eventually, because at this point all nodes in the query are known to be connected
	private String findCentroid(final List<String> query, final Map<String, Map<String, Double>> distances) {
		final double maxDistance = getMaxShortestDistance(query, distances);
		var centroid = Optional.<String>empty();
		for (var radius = maxDistance / 2; centroid.isEmpty(); ) {
			radius += maxDistance / 10;
			centroid = findCentroidWithinRadius(radius, query, distances);
		}
		return centroid.get();
	}

	private double getMaxShortestDistance(final List<String> query, final Map<String, Map<String, Double>> distances) {
		double maxDistance = 0;
		final var dijkstra = new DijkstraSearcher();
		for (int i = 0; i < query.size(); i++) {
			final var term1 = query.get(i);
			for (int j = i + 1; j < query.size(); j++) {
				final var term2 = query.get(j);
				final double distance = dijkstra.search(term1, term2, distances).getWeight();
				if (distance > maxDistance) {
					maxDistance = distance;
				}
			}
		}
		return maxDistance;
	}

	private Optional<String> findCentroidWithinRadius(final double radius, final List<String> query, final Map<String, Map<String, Double>> distances) {
		final var candidates = findCentroidCandidates(radius, query, distances);
		return getCentroidWithMinimalDistance(candidates, query);
	}

	private Map<String, Map<String, Double>> findCentroidCandidates(final double radius, final List<String> query, final Map<String, Map<String, Double>> distances) {
		final var candidates = Maps.<String, Map<String, Double>>newHashMap(query.size());
		final var bfs = new BreadthFirstGraphSearcher();
		query.forEach(q -> candidates.put(q, bfs.search(q, radius, distances)));
		return Maps.invertMapping(candidates);
	}

	private Optional<String> getCentroidWithMinimalDistance(final Map<String, Map<String, Double>> candidates, final List<String> query) {
		return candidates.entrySet().stream()
				.filter(e -> e.getValue().size() == query.size())
				.min(Comparator.comparingDouble(e -> e.getValue().values().stream().mapToDouble(d -> d).sum()))
				.map(Map.Entry::getKey);
	}

}

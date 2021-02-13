package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.graph.DijkstraSearcher;
import de.fernuni_hagen.kn.nlp.graph.GraphSearcher;
import de.fernuni_hagen.kn.nlp.math.WeightingFunctions;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Finds a centroid by spreading activation.
 *
 * @author Nils Wende
 */
public class CentroidBySpreadingActivation {

	/**
	 * Finds the centroid of the given query set.
	 *
	 * @param query query set (most frequent terms)
	 * @param db    DB
	 * @return the centroid or null, if the query is too diverse
	 */
	public String calculate(final List<String> query, final DBReader db) {
		final var significances = db.getSignificances(WeightingFunctions.DICE);
		final var cleanedQuery = cleanQuery(query, significances);
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
		for (final var queryTerm : query) {
			final var connected = GraphSearcher.breadthFirstSearch(queryTerm, significances);
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

	private String findCentroid(final List<String> query, final Map<String, Map<String, Double>> distances) {
		final double maxDistance = getMaxShortestDistance(query, distances);
		var radius = maxDistance / 2;
		var centroid = Optional.<String>empty();
		while (centroid.isEmpty()) {
			radius += maxDistance / 10;
			centroid = findCentroidWithinRadius(radius, query, distances);
		}
		return centroid.get();
	}

	private double getMaxShortestDistance(final List<String> query, final Map<String, Map<String, Double>> distances) {
		double maxDistance = 0;
		for (int i = 0; i < query.size(); i++) {
			final var term1 = query.get(i);
			for (int j = i + 1; j < query.size(); j++) {
				final var term2 = query.get(j);
				final double distance = new DijkstraSearcher().search(term1, term2, distances).getWeight();
				if (distance > maxDistance) {
					maxDistance = distance;
				}
			}
		}
		return maxDistance;
	}

	private Optional<String> findCentroidWithinRadius(final double radius, final List<String> query, final Map<String, Map<String, Double>> distances) {
		final var candidates = query.stream()
				.collect(Collectors.toMap(t -> t, t -> breadthFirstSearch(t, radius, distances)));
		return getCentroidWithMinimalDistance(candidates);
	}

	private Map<String, Double> breadthFirstSearch(final String start, final double radius, final Map<String, Map<String, Double>> distances) {
		final Map<String, Double> distancesToStart = new TreeMap<>();
		final Set<String> visited = distancesToStart.keySet();
		final var stack = new ArrayDeque<String>();
		distancesToStart.put(start, 0.0);
		stack.push(start);
		while (!stack.isEmpty()) {
			final var term = stack.pop();
			final var termDistance = distancesToStart.get(term);
			final var termDistances = distances.get(term);
			final var cooccs = distances.get(term).keySet();
			final var unvisited = CollectionUtils.removeAll(cooccs, visited).stream()
					.collect(Collectors.toMap(t -> t, next -> termDistance + termDistances.get(next)));
			unvisited.values().removeIf(distance -> distance > radius);
			distancesToStart.putAll(unvisited);
			unvisited.keySet().forEach(stack::push);
		}
		distancesToStart.remove(start);
		return distancesToStart;
	}

	private Optional<String> getCentroidWithMinimalDistance(final Map<String, Map<String, Double>> candidates) {
		return candidates.entrySet().stream()
				.min(Comparator.comparingDouble(e -> e.getValue().values().stream().mapToDouble(d -> d).sum()))
				.map(Map.Entry::getKey);
	}

}
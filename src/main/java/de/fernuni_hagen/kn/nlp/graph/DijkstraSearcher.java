package de.fernuni_hagen.kn.nlp.graph;

import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implements the Dijkstra search.
 *
 * @author Nils Wende
 */
public class DijkstraSearcher {

	private PriorityQueue<Triplet> prioQueue;
	private Set<String> visited;

	/**
	 * Finds the shortest paths between the given nodes.
	 * A node is mapped iff at least one path to another node exists.
	 * A mapping between two nodes exists iff a path exists.
	 *
	 * @param nodes     the nodes
	 * @param distances the full graph
	 * @return the shortest paths
	 */
	public Map<String, List<WeightedPath>> search(final List<String> nodes, final Map<String, Map<String, Double>> distances) {
		final var paths = Maps.<String, List<WeightedPath>>newHashMap(nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			final var node1 = nodes.get(i);
			for (int j = i + 1; j < nodes.size(); j++) {
				final var node2 = nodes.get(j);
				final var path = search(node1, node2, distances);
				if (path.exists()) {
					paths.computeIfAbsent(node1, x -> new ArrayList<>()).add(path);
				}
			}
		}
		return paths;
	}

	/**
	 * Finds the shortest path between start and end.
	 *
	 * @param start     start node
	 * @param end       end node
	 * @param distances the full graph
	 * @return the shortest path between start and end
	 */
	public WeightedPath search(final String start, final String end, final Map<String, Map<String, Double>> distances) {
		visited = new TreeSet<>();
		prioQueue = new PriorityQueue<>();
		final Triplet startTriplet = new Triplet(start, 0.0, null);
		prioQueue.add(startTriplet);
		while (!prioQueue.isEmpty()) {
			final Triplet currentTriplet = prioQueue.remove();
			if (currentTriplet.getNode().equals(end)) {
				return Triplet.toPath(startTriplet, currentTriplet);
			}
			visited.add(currentTriplet.getNode());
			queueUnvisitedNeighbors(currentTriplet, distances);
		}
		return new WeightedPath();
	}

	private void queueUnvisitedNeighbors(final Triplet currentTriplet, final Map<String, Map<String, Double>> distances) {
		final String currentNode = currentTriplet.getNode();
		final var neighbors = distances.getOrDefault(currentNode, Map.of());
		neighbors.entrySet().stream()
				.filter(e -> isUnvisited(e.getKey()))
				.map(e -> new Triplet(e.getKey(), e.getValue(), currentTriplet))
				.forEach(prioQueue::add);
	}

	private boolean isUnvisited(final String node) {
		return !visited.contains(node);
	}

	private static class Triplet implements Comparable<Triplet> {
		private final String node;
		private final Double distance;
		private final Triplet previousNode;

		Triplet(final String node, final Double distance, final Triplet previousNode) {
			this.node = node;
			this.distance = distance;
			this.previousNode = previousNode;
		}

		@Override
		public int compareTo(final Triplet o) {
			return distance.compareTo(o.distance);
		}

		static WeightedPath toPath(final Triplet start, final Triplet end) {
			return new WeightedPath(collectPath(start, end), streamPath(end).mapToDouble(Triplet::getDistance).sum());
		}

		private static List<String> collectPath(final Triplet start, final Triplet end) {
			final var path = Stream.concat(streamPath(end), Stream.of(start))
					.map(Triplet::getNode)
					.collect(Collectors.toList());
			Collections.reverse(path);
			return path;
		}

		private static Stream<Triplet> streamPath(final Triplet end) {
			return Stream.iterate(end, t -> t.getPreviousNode() != null, Triplet::getPreviousNode);
		}

		public String getNode() {
			return node;
		}

		public Double getDistance() {
			return distance;
		}

		public Triplet getPreviousNode() {
			return previousNode;
		}
	}

}

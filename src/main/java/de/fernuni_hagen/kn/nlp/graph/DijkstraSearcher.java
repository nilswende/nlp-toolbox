package de.fernuni_hagen.kn.nlp.graph;

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
				return createWeightedPath(startTriplet, currentTriplet);
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

	private WeightedPath createWeightedPath(final Triplet start, final Triplet end) {
		return new WeightedPath(collectPath(start, end), streamPath(end).mapToDouble(Triplet::getDistance).sum());
	}

	private List<String> collectPath(final Triplet start, final Triplet end) {
		final List<String> path = Stream.concat(streamPath(end), Stream.of(start))
				.map(Triplet::getNode)
				.collect(Collectors.toList());
		Collections.reverse(path);
		return path;
	}

	private Stream<Triplet> streamPath(final Triplet end) {
		return Stream.iterate(end, t -> t.getPreviousNode() != null, Triplet::getPreviousNode);
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

	/**
	 * Contains the shortest path from the start node to the end node and the total weight of the path.
	 */
	public static class WeightedPath {
		private final List<String> path;
		private final double weight;

		WeightedPath(final List<String> path, final double weight) {
			this.path = path;
			this.weight = weight;
		}

		WeightedPath() {
			this(List.of(), Double.POSITIVE_INFINITY);
		}

		public List<String> getPath() {
			return path;
		}

		public double getWeight() {
			return weight;
		}
	}

}

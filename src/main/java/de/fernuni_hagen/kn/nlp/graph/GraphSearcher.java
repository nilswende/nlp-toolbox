package de.fernuni_hagen.kn.nlp.graph;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Performs various search operations on a graph.
 *
 * @author Nils Wende
 */
public interface GraphSearcher {

	/**
	 * Find all connected nodes of {@code start}.
	 *
	 * @param start the start node
	 * @param graph the full graph
	 * @return {@code start} and all connected nodes
	 */
	Set<String> search(String start, Map<String, Map<String, Double>> graph);

	/**
	 * Find all connected nodes of {@code start} within a given {@code radius}.
	 *
	 * @param start     the start node
	 * @param radius    the maximum distance from {@code start} to a connected node
	 * @param distances the full graph including the weight of each edge
	 * @return {@code start} and all connected nodes with their actual distance from {@code start}
	 */
	Map<String, Double> search(String start, double radius, Map<String, Map<String, Double>> distances);

	/**
	 * Find the subgraph with the most nodes.<br>
	 * This is needed e. g. for PageRank calculations, when the complete graph has disjoint subgraphs.
	 *
	 * @param graph the full graph with its significance coefficients. The graph is then modified and all smaller subgraphs removed.
	 * @return {@code graph} again
	 */
	default Map<String, Map<String, Double>> findBiggestSubgraph(final Map<String, Map<String, Double>> graph) {
		final Set<String> visited = new TreeSet<>();
		Set<String> biggestSubgraph = Set.of();
		while (biggestSubgraph.size() < graph.size() / 2 || visited.size() < graph.size()) {
			final var subgraph = findSubgraph(visited, graph);
			if (subgraph.size() > biggestSubgraph.size()) {
				biggestSubgraph = subgraph;
			}
			visited.addAll(subgraph);
		}
		graph.keySet().retainAll(biggestSubgraph);
		return graph;
	}

	private Set<String> findSubgraph(final Set<String> exclude, final Map<String, Map<String, Double>> significances) {
		final var start = significances.keySet().stream().filter(t -> !exclude.contains(t)).findFirst().orElseThrow();
		return search(start, significances);
	}

	/**
	 * Find the largest subset of the given nodes that are interconnected.
	 *
	 * @param nodes the nodes to find
	 * @param graph the full graph
	 * @return the largest subset of the given nodes that are interconnected
	 */
	default List<String> getBestConnectedNodes(final List<String> nodes, final Map<String, Map<String, Double>> graph) {
		var subgraphNodes = List.<String>of();
		for (final var node : nodes) {
			final var connected = search(node, graph);
			final var connectedNodes = nodes.stream().filter(connected::contains).collect(Collectors.toList());
			if (connectedNodes.size() > subgraphNodes.size()) {
				subgraphNodes = connectedNodes;
			}
			if (subgraphNodes.size() == nodes.size()) {
				break;
			}
		}
		return subgraphNodes;
	}

	/**
	 * Returns true, if the given node is connected to all of the other given nodes, false otherwise.
	 *
	 * @param node          the node
	 * @param nodes         the nodes whose connection we want to check
	 * @param significances the full graph
	 * @return true, if the nodes are connected
	 */
	default boolean isConnected(final String node, final Collection<String> nodes, final Map<String, Map<String, Double>> significances) {
		final Set<String> connected = search(node, significances);
		return connected.containsAll(nodes);
	}

}

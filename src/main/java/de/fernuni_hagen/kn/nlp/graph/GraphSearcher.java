package de.fernuni_hagen.kn.nlp.graph;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Performs various search operations on a graph.
 *
 * @author Nils Wende
 */
public interface GraphSearcher {

	/**
	 * Find all connected nodes of {@code start}.
	 *
	 * @param start         the start node
	 * @param significances the full graph
	 * @return {@code start} and all connected nodes
	 */
	Set<String> search(String start, Map<String, Map<String, Double>> significances);

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
	 * @param significances the full graph with its significance coefficients. The graph is then modified and all smaller subgraphs removed.
	 * @return {@code significances} again
	 */
	default Map<String, Map<String, Double>> findBiggestSubgraph(final Map<String, Map<String, Double>> significances) {
		final Set<String> visited = new TreeSet<>();
		Set<String> biggestSubgraph = Set.of();
		while (biggestSubgraph.size() < significances.size() / 2 || visited.size() < significances.size()) {
			final var subgraph = findSubgraph(visited, significances);
			if (subgraph.size() > biggestSubgraph.size()) {
				biggestSubgraph = subgraph;
			}
			visited.addAll(subgraph);
		}
		significances.keySet().retainAll(biggestSubgraph);
		return significances;
	}

	private Set<String> findSubgraph(final Set<String> exclude, final Map<String, Map<String, Double>> significances) {
		final var start = significances.keySet().stream().filter(t -> !exclude.contains(t)).findFirst().orElseThrow();
		return search(start, significances);
	}

	/**
	 * Returns if the given node is connected to all of the other given nodes.
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

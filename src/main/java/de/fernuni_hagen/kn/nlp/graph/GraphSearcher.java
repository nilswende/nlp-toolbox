package de.fernuni_hagen.kn.nlp.graph;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Performs various operations on a graph.
 *
 * @author Nils Wende
 */
public class GraphSearcher {

	/**
	 * Find the subgraph with the most nodes.<br>
	 * This is needed e. g. for PageRank calculations, when the complete graph has disjoint subgraphs.
	 *
	 * @param significances the full graph with its significance coefficients. The graph is then modified and all smaller subgraphs removed.
	 */
	public static void findBiggestSubgraph(final Map<String, Map<String, Double>> significances) {
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
	}

	private static Set<String> findSubgraph(final Set<String> exclude, final Map<String, Map<String, Double>> significances) {
		final var start = significances.keySet().stream().filter(t -> !exclude.contains(t)).findFirst().orElseThrow();
		return breadthFirstSearch(start, significances);
	}

	/**
	 * Find all connected nodes of {@code start}.
	 *
	 * @param start         the start node
	 * @param significances the full graph
	 * @return {@code start} and all connected nodes
	 */
	public static Set<String> breadthFirstSearch(final String start, final Map<String, Map<String, Double>> significances) {
		final Set<String> visited = new TreeSet<>();
		final var stack = new ArrayDeque<String>();
		visited.add(start);
		stack.push(start);
		while (!stack.isEmpty()) {
			final var term = stack.pop();
			final var cooccs = significances.get(term).keySet();
			final var unvisited = CollectionUtils.removeAll(cooccs, visited);
			visited.addAll(unvisited);
			unvisited.forEach(stack::push);
		}
		return visited;
	}

	/**
	 * Returns if the given node is connected to all of the other given nodes.
	 *
	 * @param node          the node
	 * @param nodes         the nodes whose connection we want to check
	 * @param significances the full graph
	 * @return true, if the nodes are connected
	 */
	public static boolean isConnected(final String node, final Collection<String> nodes, final Map<String, Map<String, Double>> significances) {
		final Set<String> connected = breadthFirstSearch(node, significances);
		return connected.containsAll(nodes);
	}

}

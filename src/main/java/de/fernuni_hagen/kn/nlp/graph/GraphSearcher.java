package de.fernuni_hagen.kn.nlp.graph;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayDeque;
import java.util.Deque;
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

	// uses breadth-first search
	private static Set<String> findSubgraph(final Set<String> exclude, final Map<String, Map<String, Double>> significances) {
		final Set<String> terms = new TreeSet<>();
		final Deque<String> stack = new ArrayDeque<>();
		final var first = significances.keySet().stream().filter(t -> !exclude.contains(t)).findFirst().orElse(null);
		terms.add(first);
		stack.push(first);
		while (!stack.isEmpty()) {
			final var term = stack.pop();
			final var cooccs = significances.get(term).keySet();
			final var unvisited = CollectionUtils.removeAll(cooccs, terms);
			terms.addAll(unvisited);
			unvisited.forEach(stack::push);
		}
		return terms;
	}

}

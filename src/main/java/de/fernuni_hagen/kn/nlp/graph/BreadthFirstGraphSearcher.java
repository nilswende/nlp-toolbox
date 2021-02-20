package de.fernuni_hagen.kn.nlp.graph;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Performs various operations on a graph using breadth first search.
 *
 * @author Nils Wende
 */
public class BreadthFirstGraphSearcher implements GraphSearcher {

	@Override
	public Set<String> search(final String start, final Map<String, Map<String, Double>> significances) {
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

	@Override
	public Map<String, Double> search(final String start, final double radius, final Map<String, Map<String, Double>> distances) {
		final Map<String, Double> distancesToStart = new TreeMap<>();
		final Set<String> visited = distancesToStart.keySet();
		final var stack = new ArrayDeque<String>();
		distancesToStart.put(start, 0.0);
		stack.push(start);
		while (!stack.isEmpty()) {
			final var term = stack.pop();
			final var termDistance = distancesToStart.get(term);

			final var unvisited = new HashMap<>(distances.get(term));
			unvisited.keySet().removeAll(visited);
			unvisited.replaceAll((k, cooccDistance) -> termDistance + cooccDistance);
			unvisited.values().removeIf(distance -> distance > radius);

			distancesToStart.putAll(unvisited);
			unvisited.keySet().forEach(stack::push);
		}
		return distancesToStart;
	}

}

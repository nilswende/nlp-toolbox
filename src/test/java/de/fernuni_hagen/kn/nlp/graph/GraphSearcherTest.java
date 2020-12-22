package de.fernuni_hagen.kn.nlp.graph;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Nils Wende
 */
class GraphSearcherTest {

	@Test
	void findBiggestSubgraphEmpty() {
		final Map<String, List<String>> adjacencyList = Map.of();
		final var expected = toSigMap(adjacencyList);
		final var actual = new TreeMap<>(expected);
		new GraphSearcher().findBiggestSubgraph(actual);
		assertEquals(expected, actual);
	}

	@Test
	void findBiggestSubgraphEquals() {
		final var adjacencyList = Map.of(
				"A", List.of("B", "C"),
				"B", List.of("A", "D"),
				"C", List.of("A", "D"),
				"D", List.of("B", "C", "E"),
				"E", List.of("D")
		);
		final var expected = toSigMap(adjacencyList);
		final var actual = new TreeMap<>(expected);
		new GraphSearcher().findBiggestSubgraph(actual);
		assertEquals(expected, actual);
	}

	@Test
	void findBiggestSubgraphSmaller() {
		final var adjacencyList = Map.of(
				"A", List.of("B", "C"),
				"B", List.of("A", "D"),
				"C", List.of("A", "D"),
				"D", List.of("B", "C", "E"),
				"E", List.of("D"),
				"F", List.of("G"),
				"G", List.of("F")
		);
		final var expected = toSigMap(adjacencyList);
		final var actual = new TreeMap<>(expected);
		new GraphSearcher().findBiggestSubgraph(actual);
		assertNotEquals(expected, actual);
		assertEquals(Set.of("A", "B", "C", "D", "E"), actual.keySet());
	}

	@Test
	void findBiggestSubgraphSmallerFirst() {
		final var adjacencyList = Map.of(
				"A", List.of("B"),
				"B", List.of("A"),
				"C", List.of("D"),
				"D", List.of("C"),
				"E", List.of("F", "G"),
				"F", List.of("E", "G"),
				"G", List.of("E", "F")
		);
		final var expected = toSigMap(adjacencyList);
		final var actual = new TreeMap<>(expected);
		new GraphSearcher().findBiggestSubgraph(actual);
		assertNotEquals(expected, actual);
		assertEquals(Set.of("E", "F", "G"), actual.keySet());
	}

	private Map<String, Map<String, Double>> toSigMap(final Map<String, List<String>> adjacencyList) {
		final var sigs = new TreeMap<String, Map<String, Double>>();
		adjacencyList.forEach(
				(k, v) -> v.forEach(
						c -> sigs.computeIfAbsent(k, x -> new TreeMap<>()).put(c, 0.0)
				));
		return sigs;
	}
}

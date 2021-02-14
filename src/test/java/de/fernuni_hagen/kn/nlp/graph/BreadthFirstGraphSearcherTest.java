package de.fernuni_hagen.kn.nlp.graph;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nils Wende
 */
class BreadthFirstGraphSearcherTest {

	@Test
	void findBiggestSubgraphEmpty() {
		final Map<String, List<String>> adjacencyList = Map.of();
		testEquality(adjacencyList);
	}

	@Test
	void findBiggestSubgraphSingle() {
		final var adjacencyList = Map.of(
				"A", List.<String>of()
		);
		testEquality(adjacencyList);
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
		testEquality(adjacencyList);
	}

	private void testEquality(final Map<String, List<String>> adjacencyList) {
		final var expected = toSigMap(adjacencyList);
		final var actual = new BreadthFirstGraphSearcher().findBiggestSubgraph(new TreeMap<>(expected));
		assertEquals(expected, actual);
	}

	private Map<String, Map<String, Double>> toSigMap(final Map<String, List<String>> adjacencyList) {
		final var sigs = new TreeMap<String, Map<String, Double>>();
		adjacencyList.forEach(
				(k, v) -> v.forEach(
						c -> sigs.computeIfAbsent(k, x -> new TreeMap<>()).put(c, 0.0)
				));
		return sigs;
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
		testInequality(Set.of("A", "B", "C", "D", "E"), adjacencyList);
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
		testInequality(Set.of("E", "F", "G"), adjacencyList);
	}

	@Test
	void findBiggestSubgraphSmallerFirstUnderHalf() {
		final var adjacencyList = Map.of(
				"A", List.of("B"),
				"B", List.of("A"),
				"C", List.of("D", "E"),
				"D", List.of("C", "E"),
				"E", List.of("C", "D")
		);
		testInequality(Set.of("C", "D", "E"), adjacencyList);
	}

	private void testInequality(final Set<String> expectedTerms, final Map<String, List<String>> adjacencyList) {
		final var expected = toSigMap(adjacencyList);
		final var actual = new BreadthFirstGraphSearcher().findBiggestSubgraph(new TreeMap<>(expected));
		assertNotEquals(expected, actual);
		assertEquals(expectedTerms, actual.keySet());
		actual.values().stream().map(Map::keySet).forEach(keys -> assertTrue(expectedTerms.containsAll(keys), keys.toString()));
	}

}

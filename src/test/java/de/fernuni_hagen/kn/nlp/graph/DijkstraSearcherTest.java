package de.fernuni_hagen.kn.nlp.graph;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class DijkstraSearcherTest {

	private static final double INFINITY = Double.POSITIVE_INFINITY;

	@ParameterizedTest
	@MethodSource
	void search(final String start, final String end, final Map<String, Map<String, Double>> adjacencyList,
				final List<String> expectedPath, final double expectedDistance) {
		final var path = new DijkstraSearcher().search(start, end, adjacencyList);
		assertEquals(expectedPath, path.getPath());
		assertEquals(expectedDistance, path.getWeight(), .0001);
	}

	static Stream<Arguments> search() {
		return Stream.of(
				// path finding
				arguments("a", "b", Map.of(), List.of(), INFINITY), // empty
				arguments("a", "a", Map.of(
						"a", Map.of()
				), List.of(), 0), // self
				arguments("a", "b", Map.of(
						"a", Map.of(),
						"b", Map.of()
				), List.of(), INFINITY), // no edge
				arguments("a", "b", Map.of(
						"a", Map.of("b", 1.0),
						"b", Map.of()
				), List.of("b"), 1), // simple edge
				arguments("a", "c", Map.of(
						"a", Map.of("b", 1.0),
						"b", Map.of("c", 1.0)
				), List.of("b", "c"), 2), // transitive edge / path
				// shortest path finding
				arguments("a", "e", Map.of(
						"a", Map.of("b", 1.0),
						"b", Map.of("c", 1.0, "d", 10.0),
						"c", Map.of("e", 20.0),
						"d", Map.of("e", 1.0),
						"e", Map.of()
				), List.of("b", "d", "e"), 12),
				arguments("a", "e", Map.of(
						"a", Map.of("b", 1.0),
						"b", Map.of("c", 10.0, "d", 1.0),
						"c", Map.of("e", 1.0),
						"d", Map.of("e", 20.0),
						"e", Map.of()
				), List.of("b", "c", "e"), 12)
		);
	}

}

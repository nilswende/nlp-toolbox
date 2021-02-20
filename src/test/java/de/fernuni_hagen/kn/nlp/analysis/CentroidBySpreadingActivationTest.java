package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author Nils Wende
 */
class CentroidBySpreadingActivationTest {

	@ParameterizedTest
	@MethodSource
	void clean(final List<String> query, final Map<String, Map<String, Double>> significances,
			   final String expectedCentroid) {
		final DBReader dbReader = Mockito.mock(DBReader.class);
		Mockito.when(dbReader.getSignificances(any(WeightingFunction.class))).thenReturn(Maps.copyOf(significances));
		final String actual = new CentroidBySpreadingActivation().calculate(query, dbReader);
		assertEquals(expectedCentroid, actual);
	}

	static Stream<Arguments> clean() {
		return Stream.of(
				arguments(List.of(), Map.of(), null),
				arguments(List.of("a"), Map.of(), null),
				arguments(List.of("a"), Map.of(
						"a", Map.of()
				), null),
				arguments(List.of("a", "b"), Map.of(
						"a", Map.of()
				), null),
				arguments(List.of("a", "c"), Map.of(
						"a", Map.of("b", 0),
						"b", Map.of("a", 0),
						"c", Map.of()
				), null)
		);
	}

	@ParameterizedTest
	@MethodSource
	void calculate(final List<String> query, final Map<String, Map<String, Double>> significances,
				   final String expectedCentroid) {
		final DBReader dbReader = Mockito.mock(DBReader.class);
		Mockito.when(dbReader.getSignificances(any(WeightingFunction.class))).thenReturn(Maps.copyOf(significances));
		final String actual = new CentroidBySpreadingActivation().calculate(query, dbReader);
		assertEquals(expectedCentroid, actual);
	}

	static Stream<Arguments> calculate() {
		return Stream.of(
				arguments(List.of("a", "c"), Map.of( // query contains centroid
						"a", Map.of("b", .5, "c", .5),
						"b", Map.of("a", .5),
						"c", Map.of("a", .5)
				), "a"),
				arguments(List.of("a", "c"), Map.of( // centroid in the middle
						"a", Map.of("b", .5),
						"b", Map.of("a", .5, "c", .5),
						"c", Map.of("b", .5)
				), "b"),
				arguments(List.of("a", "d"), Map.of( // centroid is part of shortest path
						"a", Map.of("b", .8, "c", .2),
						"b", Map.of("a", .8, "d", .8),
						"c", Map.of("a", .2, "d", .2),
						"d", Map.of("b", .8, "c", .2)
				), "b"),
				arguments(List.of("a", "b", "c"), Map.of( // centroid not part of all shortest paths
						"a", Map.of("e", .1, "d", .5),
						"b", Map.of("c", .8, "d", .5),
						"c", Map.of("b", .8, "e", .8),
						"d", Map.of("a", .5, "b", .5),
						"e", Map.of("a", .1, "c", .8)
				), "d")
		);
	}

}

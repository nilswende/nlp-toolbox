package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nils Wende
 */
class PageRankTest {

	@Test
	void calculate() {
		final var dbReader = mockDbReader();
		final var mock = mock();
		mock.execute(dbReader);
		final var actual = mock.getResult().getScores();
		assertEqualSize(dbReader, actual);
		assertEqualPageRank(actual, "a", "b");
		assertMaxPageRank(actual, "c");
		assertMinPageRank(actual, "d");
	}

	private DBReader mockDbReader() {
		final var map = Map.of(
				"a", Map.of("b", 1.0, "c", 1.0),
				"b", Map.of("a", 1.0, "c", 1.0),
				"c", Map.of("a", 1.0, "b", 1.0, "d", 1.0),
				"d", Map.of("c", 1.0)
		);
		final DBReader dbReader = Mockito.mock(DBReader.class);
		Mockito.when(dbReader.getSignificances(ArgumentMatchers.any(WeightingFunction.class))).thenReturn(Maps.copyOf(map));
		return dbReader;
	}

	private PageRank mock() {
		return new PageRank()
				.setIterations(25)
				.setWeight(.85)
				.setWeightingFunction(WeightingFunction.NONE);
	}

	private void assertEqualSize(final DBReader dbReader, final Map<String, Double> actual) {
		assertEquals(dbReader.getSignificances(WeightingFunction.NONE).size(), actual.size());
	}

	private void assertEqualPageRank(final Map<String, Double> actual, final String a, final String b) {
		assertEquals(actual.get(a), actual.get(b), .001);
	}

	private void assertMaxPageRank(final Map<String, Double> actual, final String node) {
		assertEquals(node, actual.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElseThrow());
	}

	private void assertMinPageRank(final Map<String, Double> actual, final String node) {
		assertEquals(node, actual.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElseThrow());
	}

}

package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.PageRankConfig;
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
		final PageRankConfig config = mockConfig();
		final var dbReader = mockDbReader();
		final var actual = new PageRank(config).calculate(dbReader);
		assertEqualSize(dbReader, actual);
		assertEqualPageRank(actual, "a", "b");
		assertMaxPageRank(actual, "c");
		assertMinPageRank(actual, "d");
	}

	private DBReader mockDbReader() {
		final var map = Map.of(
				"a", Map.of("b", .0, "c", .0),
				"b", Map.of("a", .0, "c", .0),
				"c", Map.of("a", .0, "b", .0, "d", .0),
				"d", Map.of("c", .0)
		);
		final DBReader dbReader = Mockito.mock(DBReader.class);
		Mockito.when(dbReader.getSignificances(ArgumentMatchers.any(WeightingFunction.class))).thenReturn(Maps.copyOf(map));
		return dbReader;
	}

	private PageRankConfig mockConfig() {
		final var config = Mockito.mock(PageRankConfig.class);
		Mockito.when(config.calculate()).thenReturn(true);
		Mockito.when(config.getIterations()).thenReturn(25);
		Mockito.when(config.getResultLimit()).thenReturn(Integer.MAX_VALUE);
		Mockito.when(config.getWeight()).thenReturn(.85);
		Mockito.when(config.getWeightingFunction()).thenReturn(WeightingFunction.DICE);
		return config;
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

package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.HITSConfig;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nils Wende
 */
class HITSTest {

	@Test
	void calculate() {
		final var config = mockConfig();
		final var dbReader = mockDbReader();
		final var actual = new HITS(config).calculate(dbReader);
		assertEqualSize(dbReader, actual);
		assertEqualScore("a", "b", actual, HITS.Scores::getHubScore);
		assertEqualScore("a", "b", actual, HITS.Scores::getAuthorityScore);
		assertMaxScore("c", actual, HITS.Scores::getHubScore);
		assertMaxScore("c", actual, HITS.Scores::getAuthorityScore);
		assertMinScore("d", actual, HITS.Scores::getHubScore);
		assertMinScore("d", actual, HITS.Scores::getAuthorityScore);
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

	private HITSConfig mockConfig() {
		final var config = Mockito.mock(HITSConfig.class);
		Mockito.when(config.calculate()).thenReturn(true);
		Mockito.when(config.directed()).thenReturn(true);
		Mockito.when(config.getIterations()).thenReturn(50);
		Mockito.when(config.getResultLimit()).thenReturn(Integer.MAX_VALUE);
		Mockito.when(config.getWeightingFunction()).thenReturn(WeightingFunction.NONE);
		return config;
	}

	private void assertEqualSize(final DBReader dbReader, final Map<String, HITS.Scores> actual) {
		assertEquals(dbReader.getSignificances(WeightingFunction.NONE).size(), actual.size());
	}

	private void assertEqualScore(final String a, final String b,
								  final Map<String, HITS.Scores> actual,
								  final Function<HITS.Scores, Double> score) {
		assertEquals(score.apply(actual.get(a)), score.apply(actual.get(b)), .001);
	}

	private void assertMaxScore(final String node,
								final Map<String, HITS.Scores> actual,
								final Function<HITS.Scores, Double> score) {
		assertEquals(node, actual.entrySet().stream().max(Comparator.comparing(score.compose(Map.Entry::getValue))).map(Map.Entry::getKey).orElseThrow());
	}

	private void assertMinScore(final String node,
								final Map<String, HITS.Scores> actual,
								final Function<HITS.Scores, Double> score) {
		assertEquals(node, actual.entrySet().stream().min(Comparator.comparing(score.compose(Map.Entry::getValue))).map(Map.Entry::getKey).orElseThrow());
	}

}

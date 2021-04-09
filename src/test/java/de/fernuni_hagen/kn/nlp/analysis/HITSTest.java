package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Set;

import static java.util.Map.Entry.comparingByValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nils Wende
 */
class HITSTest {

	@Test
	void calculate() {
		final var dbReader = mockDbReader();
		final var mock = mock();
		mock.execute(dbReader);
		final var result = mock.getResult();
		assertEqualSize(dbReader, result.getTerms());
		assertEqualScore("a", "b", result.getHubScores());
		assertEqualScore("a", "b", result.getAuthorityScores());
		assertMaxScore("c", result.getHubScores());
		assertMaxScore("c", result.getAuthorityScores());
		assertMinScore("d", result.getHubScores());
		assertMinScore("d", result.getAuthorityScores());
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

	private HITS mock() {
		return new HITS()
				.setIterations(50)
				.setWeightingFunction(WeightingFunction.NONE);
	}

	private void assertEqualSize(final DBReader dbReader, final Set<String> actual) {
		assertEquals(dbReader.getSignificances(WeightingFunction.NONE).size(), actual.size());
	}

	private void assertEqualScore(final String a, final String b, final Map<String, Double> actual) {
		assertEquals(actual.get(a), actual.get(b), .001);
	}

	private void assertMaxScore(final String node,
								final Map<String, Double> actual) {
		assertEquals(node, actual.entrySet().stream().max(comparingByValue()).map(Map.Entry::getKey).orElseThrow());
	}

	private void assertMinScore(final String node,
								final Map<String, Double> actual) {
		assertEquals(node, actual.entrySet().stream().min(comparingByValue()).map(Map.Entry::getKey).orElseThrow());
	}

}

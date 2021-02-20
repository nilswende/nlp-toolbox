package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.DocSimConfig;
import de.fernuni_hagen.kn.nlp.math.DocSimilarityFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nils Wende
 */
class DocumentSimilarityTest {

	@Test
	void calculate() {
		final DBReader dbReader = mockDbReader();
		final DocSimConfig config = mockConfig("1", "2", "3", "4");

		final var actual = new DocumentSimilarity(config).calculate(dbReader);
		assertFullCalculation(actual);
	}

	private DBReader mockDbReader() {
		final var map = Map.of(
				"Vertrag", Map.of("1", 3L),
				"riskant", Map.of("2", 1L),
				"Weg", Map.of("2", 1L, "3", 1L),
				"groß", Map.of("2", 1L),
				"Profit", Map.of("2", 1L, "3", 1L, "4", 2L),
				"führen", Map.of("3", 1L),
				"Rechtfertigung", Map.of("4", 1L)
		);
		final DBReader dbReader = Mockito.mock(DBReader.class);
		Mockito.when(dbReader.getTermFrequencies()).thenReturn(Maps.copyOf(map));
		return dbReader;
	}

	private DocSimConfig mockConfig(final String... docs) {
		final DocSimConfig config = Mockito.mock(DocSimConfig.class);
		Mockito.when(config.calculate()).thenReturn(true);
		Mockito.when(config.getDocuments()).thenReturn(List.of(docs));
		Mockito.when(config.getWeightThreshold()).thenReturn(0.001);
		Mockito.when(config.useInverseDocFrequency()).thenReturn(true);
		Mockito.when(config.getSimilarityFunction()).thenReturn(DocSimilarityFunction.COSINE);
		return config;
	}

	private void assertFullCalculation(final Map<String, Map<String, Double>> actual) {
		final var expected = Map.of(
				"1", Map.of("2", .0, "3", .0, "4", .0),
				"2", Map.of("1", .0, "3", .169, "4", .052),
				"3", Map.of("1", .0, "2", .169, "4", .07),
				"4", Map.of("1", .0, "2", .052, "3", .07)
		);
		assertEquals(expected.size() - 1, actual.size(), "actual contains too many entries");
		expected.forEach((d1, m) -> m.forEach((d2, s) ->
				assertEquals(s, getActualSim(d1, d2, actual), .0015, "d1=" + d1 + ", d2=" + d2)
		));
	}

	private double getActualSim(final String d1, final String d2, final Map<String, Map<String, Double>> actual) {
		return actual.containsKey(d1) && actual.get(d1).containsKey(d2) ? actual.get(d1).get(d2) : actual.get(d2).get(d1);
	}

	@Test
	void calculateSubsetDocs() {
		final DBReader dbReader = mockDbReader();
		final DocSimConfig config = mockConfig("1", "2", "3");

		final var actual = new DocumentSimilarity(config).calculate(dbReader);
		final var expected = Map.of(
				"1", Map.of("2", .0, "3", .0),
				"2", Map.of("1", .0, "3", .160),
				"3", Map.of("1", .0, "2", .160)
		);
		assertEquals(expected.size() - 1, actual.size(), "actual contains too many entries");
		expected.forEach((d1, m) -> m.forEach((d2, s) ->
				assertEquals(s, getActualSim(d1, d2, actual), .0015, "d1=" + d1 + ", d2=" + d2)
		));
	}

	@Test
	void calculateStandardDocs() {
		final DBReader dbReader = mockDbReader();
		final DocSimConfig config = mockConfig();

		final var actual = new DocumentSimilarity(config).calculate(dbReader);
		assertFullCalculation(actual);
	}

}
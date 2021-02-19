package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.DocSimConfig;
import de.fernuni_hagen.kn.nlp.math.DocSimilarityFunction;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		final var map = new MultiKeyMap<String, Double>();
		map.putAll(Map.of(
				new MultiKey<>("Vertrag", "1"), 3.0,
				new MultiKey<>("riskant", "2"), 1.0,
				new MultiKey<>("Weg", "2"), 1.0,
				new MultiKey<>("Weg", "3"), 1.0,
				new MultiKey<>("groß", "2"), 1.0,
				new MultiKey<>("Profit", "2"), 1.0,
				new MultiKey<>("Profit", "3"), 1.0,
				new MultiKey<>("Profit", "4"), 2.0,
				new MultiKey<>("führen", "3"), 1.0,
				new MultiKey<>("Rechtfertigung", "4"), 1.0
		));
		final DBReader dbReader = Mockito.mock(DBReader.class);
		Mockito.when(dbReader.getTermFrequencies()).thenReturn(map);
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

	private void assertFullCalculation(final MultiKeyMap<String, Double> actual) {
		final var expected = new MultiKeyMap<String, Double>();
		expected.putAll(Map.ofEntries(
				Map.entry(new MultiKey<>("1", "2"), .0),
				Map.entry(new MultiKey<>("1", "3"), .0),
				Map.entry(new MultiKey<>("1", "4"), .0),
				Map.entry(new MultiKey<>("2", "1"), .0),
				Map.entry(new MultiKey<>("2", "3"), .169),
				Map.entry(new MultiKey<>("2", "4"), .052),
				Map.entry(new MultiKey<>("3", "1"), .0),
				Map.entry(new MultiKey<>("3", "2"), .169),
				Map.entry(new MultiKey<>("3", "4"), .07),
				Map.entry(new MultiKey<>("4", "1"), .0),
				Map.entry(new MultiKey<>("4", "2"), .052),
				Map.entry(new MultiKey<>("4", "3"), .07)
		));
		assertEqual(actual, expected);
	}

	private double getActualSim(final String d1, final String d2, final MultiKeyMap<String, Double> actual) {
		return actual.containsKey(d1, d2) ? actual.get(d1, d2) : actual.get(d2, d1);
	}

	@Test
	void calculateSubsetDocs() {
		final DBReader dbReader = mockDbReader();
		final DocSimConfig config = mockConfig("1", "2", "3");

		final var actual = new DocumentSimilarity(config).calculate(dbReader);
		final var expected = new MultiKeyMap<String, Double>();
		expected.putAll(Map.of(
				new MultiKey<>("1", "2"), .0,
				new MultiKey<>("1", "3"), .0,
				new MultiKey<>("2", "1"), .0,
				new MultiKey<>("2", "3"), .160,
				new MultiKey<>("3", "1"), .0,
				new MultiKey<>("3", "2"), .160
		));
		assertEqual(actual, expected);
	}

	private void assertEqual(final MultiKeyMap<String, Double> actual, final MultiKeyMap<String, Double> expected) {
		assertTrue(expected.size() >= actual.size(), "actual contains too many entries");
		expected.forEach((k, s) -> {
					final var d1 = k.getKey(0);
					final var d2 = k.getKey(1);
					assertEquals(s, getActualSim(d1, d2, actual), .0015, "d1=" + d1 + ", d2=" + d2);
				}
		);
	}

	@Test
	void calculateStandardDocs() {
		final DBReader dbReader = mockDbReader();
		final DocSimConfig config = mockConfig();

		final var actual = new DocumentSimilarity(config).calculate(dbReader);
		assertFullCalculation(actual);
	}

}

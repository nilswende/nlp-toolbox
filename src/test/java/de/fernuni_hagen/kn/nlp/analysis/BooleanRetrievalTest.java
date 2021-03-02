package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.utils.Maps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Nils Wende
 */
class BooleanRetrievalTest {

	private final DBReader dbReader = mockDbReader();

	private DBReader mockDbReader() {
		final var map = Map.of(
				"t1", Map.of("d1", 1L),
				"t2", Map.of("d1", 1L, "d2", 1L),
				"t3", Map.of("d2", 1L)
		);
		final DBReader dbReader = Mockito.mock(DBReader.class);
		Mockito.when(dbReader.getTermFrequencies()).thenReturn(Maps.copyOf(map));
		return dbReader;
	}

	private BooleanRetrieval.Config mockConfig(final String... query) {
		final BooleanRetrieval.Config config = Mockito.mock(BooleanRetrieval.Config.class);
		Mockito.when(config.getQuery()).thenReturn(List.of(query));
		return config;
	}

	@Test
	void and() {
		final var config = mockConfig("t1", "t2");
		final var actual = new BooleanRetrieval(config).and(dbReader);
		Assertions.assertEquals(Set.of("d1"), actual);
	}

	@Test
	void or() {
		final var config = mockConfig("t1", "t2");
		final var actual = new BooleanRetrieval(config).or(dbReader);
		Assertions.assertEquals(2L, actual.get("d1"));
		Assertions.assertEquals(1L, actual.get("d2"));
	}

	@Test
	void not() {
		final var config = mockConfig("t1");
		final var actual = new BooleanRetrieval(config).not(dbReader);
		Assertions.assertEquals(Set.of("d2"), actual);
	}
}

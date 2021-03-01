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
		final var dbReader = Mockito.mock(DBReader.class);
		Mockito.when(dbReader.getTermFrequencies()).thenReturn(Maps.copyOf(map));
		return dbReader;
	}

	private BooleanRetrieval mock(List<String> query) {
		final var booleanRetrieval = new BooleanRetrieval();
		booleanRetrieval.setQuery(query);
		return booleanRetrieval;
	}

	@Test
	void and() {
		final BooleanRetrieval booleanRetrieval = mock(List.of("t1", "t2"));
		final var actual = booleanRetrieval.and(dbReader);
		Assertions.assertEquals(Set.of("d1"), actual);
	}

	@Test
	void or() {
		final var query = List.of("t1", "t2");
		final BooleanRetrieval booleanRetrieval = mock(query);
		final var actual = booleanRetrieval.or(dbReader);
		Assertions.assertEquals(2L, actual.get("d1"));
		Assertions.assertEquals(1L, actual.get("d2"));
	}

	@Test
	void not() {
		final BooleanRetrieval booleanRetrieval = mock(List.of("t1"));
		final var actual = booleanRetrieval.not(dbReader);
		Assertions.assertEquals(Set.of("d2"), actual);
	}
}

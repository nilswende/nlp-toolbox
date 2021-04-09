package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.utils.Maps;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

	@Test
	void and() {
		final var booleanRetrieval = new BooleanRetrieval()
				.setExpression("and")
				.setQuery(List.of("t1", "t2"));
		booleanRetrieval.execute(dbReader);
		final var result = booleanRetrieval.getResult();
		assertEquals(Set.of("d1"), result.getDocuments().keySet());
	}

	@Test
	void or() {
		final var booleanRetrieval = new BooleanRetrieval()
				.setExpression("or")
				.setQuery(List.of("t1", "t2"));
		booleanRetrieval.execute(dbReader);
		final var result = booleanRetrieval.getResult();
		assertEquals(2L, result.getDocuments().get("d1"));
		assertEquals(1L, result.getDocuments().get("d2"));
	}

	@Test
	void not() {
		final var booleanRetrieval = new BooleanRetrieval()
				.setExpression("not")
				.setQuery(List.of("t1"));
		booleanRetrieval.execute(dbReader);
		final var result = booleanRetrieval.getResult();
		assertEquals(Set.of("d2"), result.getDocuments().keySet());
	}
}

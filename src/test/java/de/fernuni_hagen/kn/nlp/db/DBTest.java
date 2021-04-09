package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Initializes the DB before the first test class and resets it for each test.
 *
 * @author Nils Wende
 */
public abstract class DBTest {

	protected DBReader reader = getDbFactory().getReader();
	protected DBWriter writer = getDbFactory().getWriter();

	@BeforeEach
	void beforeEach() {
		writer.deleteAll();
		writer.addDocument("1");
	}

	@AfterEach
	void afterEach() {
		writer.deleteAll();
	}

	protected abstract DBFactory getDbFactory();

	@Test
	void getCooccurrences() {
		writer.addSentence(List.of("a", "b", "c", "e"));
		writer.addSentence(List.of("b", "d", "c", "d"));

		final var cooccurrences = reader.getCooccurrences();
		assertEquals(5, cooccurrences.size(), cooccurrences.toString());
		var cooccs = cooccurrences.get("a");
		assertEquals(3, cooccs.size(), cooccs.toString());
		assertEquals(1, cooccs.get("b"));
		assertEquals(1, cooccs.get("c"));
		assertEquals(1, cooccs.get("e"));
		cooccs = cooccurrences.get("b");
		assertEquals(4, cooccs.size(), cooccs.toString());
		assertEquals(1, cooccs.get("a"));
		assertEquals(2, cooccs.get("c"));
		assertEquals(2, cooccs.get("d"));
		assertEquals(1, cooccs.get("e"));
		cooccs = cooccurrences.get("c");
		assertEquals(4, cooccs.size(), cooccs.toString());
		assertEquals(1, cooccs.get("a"));
		assertEquals(2, cooccs.get("b"));
		assertEquals(2, cooccs.get("d"));
		assertEquals(1, cooccs.get("e"));
		cooccs = cooccurrences.get("d");
		assertEquals(3, cooccs.size(), cooccs.toString());
		assertEquals(2, cooccs.get("b"));
		assertEquals(2, cooccs.get("c"));
		assertEquals(1, cooccs.get("d"));
		cooccs = cooccurrences.get("e");
		assertEquals(3, cooccs.size(), cooccs.toString());
		assertEquals(1, cooccs.get("a"));
		assertEquals(1, cooccs.get("b"));
		assertEquals(1, cooccs.get("c"));
	}

	@Test
	void getSignificances() {
		final var input = List.of("art", "competition", "game", "year");
		writer.addSentence(input);

		final var significances = reader.getSignificances(WeightingFunction.NONE);
		assertEquals(Set.copyOf(input), significances.keySet());
		significances.forEach((k, v) -> {
			final var terms = new TreeSet<>(input);
			terms.remove(k);
			final var cooccs = v.keySet();
			assertEquals(terms, cooccs);
		});
	}

	@Test
	void testDirectedSignificances() {
		final var input = List.of("art", "competition", "game", "year");
		writer.addSentence(input);

		final var significances = reader.getDirectedSignificances(WeightingFunction.NONE);
		assertOneDirectedRelationshipBetweenTwoNodes(significances);
	}

	private void assertOneDirectedRelationshipBetweenTwoNodes(final Map<String, Map<String, Double>> significances) {
		significances.forEach(
				(k, v) -> v.keySet().forEach(
						coocc -> assertTrue(!significances.containsKey(coocc) || !significances.get(coocc).containsKey(k))
				));
	}

	@Test
	void getTermFrequencies() {
		writer.addSentence(List.of("a", "b", "c"));
		writer.addSentence(List.of("b", "d", "c", "e"));
		writer.addDocument("2");
		writer.addSentence(List.of("b", "a"));
		writer.addSentence(List.of("a", "f"));

		final var frequencies = reader.getTermFrequencies();
		assertEquals(2, frequencies.size(), frequencies.toString());
		var doc = frequencies.get("1");
		assertEquals(5, doc.size(), doc.toString());
		assertEquals(1, doc.get("a"));
		assertEquals(2, doc.get("b"));
		assertEquals(2, doc.get("c"));
		assertEquals(1, doc.get("d"));
		assertEquals(1, doc.get("e"));
		doc = frequencies.get("2");
		assertEquals(3, doc.size(), doc.toString());
		assertEquals(2, doc.get("a"));
		assertEquals(1, doc.get("b"));
		assertEquals(1, doc.get("f"));
	}

	@Test
	void getShortestPath() {//TODO
	}

	@Test
	void getAllSentencesInDocument() {
		writer.addSentence(List.of("a", "b", "c", "d"));
		writer.addSentence(List.of("e", "f", "g", "h"));

		final var list = reader.getAllSentencesInDocument("1");
		assertEquals(2, list.size(), list.toString());
		var sentence = list.get(0);
		assertEquals(4, sentence.size(), sentence.toString());
		assertEquals("a", sentence.get(0));
		assertEquals("b", sentence.get(1));
		assertEquals("c", sentence.get(2));
		assertEquals("d", sentence.get(3));
		sentence = list.get(1);
		assertEquals(4, sentence.size(), sentence.toString());
		assertEquals("e", sentence.get(0));
		assertEquals("f", sentence.get(1));
		assertEquals("g", sentence.get(2));
		assertEquals("h", sentence.get(3));
	}

}

package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.TempFileTest;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Initializes the DB before the first test class and resets it for each test.
 *
 * @author Nils Wende
 */
public abstract class DBTest extends TempFileTest {

	protected DBReader reader;
	protected DBWriter writer;

	protected static DBFactory createDbFactory(AppConfig.DbType dbType) {
		final var mock = new AppConfig()
				.setDb(dbType)
				.setDbDir(tempFile.getParent().toString())
				.setPersistInMemoryDb(true);
		return DBFactory.from(mock);
	}

	@BeforeEach
	void beforeEach() {
		reader = getDbFactory().getReader();
		writer = getDbFactory().getWriter();
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
		getCooccs(cooccurrences::get);
	}

	private void getCooccs(final Function<String, Map<String, Double>> f) {
		var cooccs = f.apply("a");
		assertEquals(3, cooccs.size(), cooccs.toString());
		assertEquals(1, cooccs.get("b"));
		assertEquals(1, cooccs.get("c"));
		assertEquals(1, cooccs.get("e"));
		cooccs = f.apply("b");
		assertEquals(4, cooccs.size(), cooccs.toString());
		assertEquals(1, cooccs.get("a"));
		assertEquals(2, cooccs.get("c"));
		assertEquals(2, cooccs.get("d"));
		assertEquals(1, cooccs.get("e"));
		cooccs = f.apply("c");
		assertEquals(4, cooccs.size(), cooccs.toString());
		assertEquals(1, cooccs.get("a"));
		assertEquals(2, cooccs.get("b"));
		assertEquals(2, cooccs.get("d"));
		assertEquals(1, cooccs.get("e"));
		cooccs = f.apply("d");
		assertEquals(2, cooccs.size(), cooccs.toString());
		assertEquals(2, cooccs.get("b"));
		assertEquals(2, cooccs.get("c"));
		cooccs = f.apply("e");
		assertEquals(3, cooccs.size(), cooccs.toString());
		assertEquals(1, cooccs.get("a"));
		assertEquals(1, cooccs.get("b"));
		assertEquals(1, cooccs.get("c"));
	}

	@Test
	void getCooccurrencesTerm() {
		writer.addSentence(List.of("a", "b", "c", "e"));
		writer.addSentence(List.of("b", "d", "c", "d"));

		getCooccs(reader::getCooccurrences);
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
		writer.addSentence(List.of("car", "brand1"));
		writer.addSentence(List.of("car", "brand2"));

		final var significances = reader.getDirectedSignificances(WeightingFunction.NONE);
		assertTrue(significances.get("car").get("brand1") < significances.get("brand1").get("car"), significances.toString());
	}

	@Test
	void getTermFrequencies() {
		writer.addSentence(List.of("a", "b", "c"));
		writer.addSentence(List.of("b", "d", "c", "e"));
		writer.addDocument("2");
		writer.addSentence(List.of("b", "a"));
		writer.addSentence(List.of("a", "f"));

		final var term2doc = reader.getTermFrequencies();
		assertEquals(6, term2doc.size(), term2doc.toString());
		var term = term2doc.get("a");
		assertEquals(2, term.size(), term.toString());
		assertEquals(1, term.get("1"));
		assertEquals(2, term.get("2"));
		term = term2doc.get("b");
		assertEquals(2, term.size(), term.toString());
		assertEquals(2, term.get("1"));
		assertEquals(1, term.get("2"));
		term = term2doc.get("c");
		assertEquals(1, term.size(), term.toString());
		assertEquals(2, term.get("1"));
		term = term2doc.get("d");
		assertEquals(1, term.size(), term.toString());
		assertEquals(1, term.get("1"));
		term = term2doc.get("e");
		assertEquals(1, term.size(), term.toString());
		assertEquals(1, term.get("1"));
		term = term2doc.get("f");
		assertEquals(1, term.size(), term.toString());
		assertEquals(1, term.get("2"));
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

	@Test
	void containsTerms() {
		writer.addSentence(List.of("a", "b", "c", "d"));
		writer.addSentence(List.of("e", "f", "g", "h"));

		assertTrue(reader.containsTerms(List.of("a", "f")));
		assertFalse(reader.containsTerms(List.of("a", "f", "x")));
	}

	@Test
	void containsTerm() {
		writer.addSentence(List.of("a", "b", "c", "d"));
		writer.addSentence(List.of("e", "f", "g", "h"));

		assertTrue(reader.containsTerm("a"));
		assertTrue(reader.containsTerm("f"));
		assertFalse(reader.containsTerm("x"));
	}

}

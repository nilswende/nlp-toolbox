package de.fernuni_hagen.kn.nlp.db.neo4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Nils Wende
 */
class Neo4JReaderTest extends DBTestNeo4J {

	@Test
	void getTermFrequencies() {
		writer.addSentence(List.of("a", "b", "c"));
		writer.addSentence(List.of("b", "d", "c", "e"));
		writer.addDocument(Path.of("2"));
		writer.addSentence(List.of("b", "a"));
		writer.addSentence(List.of("a", "f"));

		final var frequencies = reader.getTermFrequencies();
		Assertions.assertEquals(2, frequencies.size(), frequencies.toString());
		var doc = frequencies.get("1");
		Assertions.assertEquals(5, doc.size(), doc.toString());
		Assertions.assertEquals(1, doc.get("a"));
		Assertions.assertEquals(2, doc.get("b"));
		Assertions.assertEquals(2, doc.get("c"));
		Assertions.assertEquals(1, doc.get("d"));
		Assertions.assertEquals(1, doc.get("e"));
		doc = frequencies.get("2");
		Assertions.assertEquals(3, doc.size(), doc.toString());
		Assertions.assertEquals(2, doc.get("a"));
		Assertions.assertEquals(1, doc.get("b"));
		Assertions.assertEquals(1, doc.get("f"));
	}

	@Test
	void getAllSentencesInDocument() {
		writer.addSentence(List.of("a", "b", "c", "d"));
		writer.addSentence(List.of("e", "f", "g", "h"));

		final var list = reader.getAllSentencesInDocument(Path.of("1"));
		Assertions.assertEquals(2, list.size(), list.toString());
		var sentence = list.get(0);
		Assertions.assertEquals(4, sentence.size(), sentence.toString());
		Assertions.assertEquals("a", sentence.get(0));
		Assertions.assertEquals("b", sentence.get(1));
		Assertions.assertEquals("c", sentence.get(2));
		Assertions.assertEquals("d", sentence.get(3));
		sentence = list.get(1);
		Assertions.assertEquals(4, sentence.size(), sentence.toString());
		Assertions.assertEquals("e", sentence.get(0));
		Assertions.assertEquals("f", sentence.get(1));
		Assertions.assertEquals("g", sentence.get(2));
		Assertions.assertEquals("h", sentence.get(3));
	}
}

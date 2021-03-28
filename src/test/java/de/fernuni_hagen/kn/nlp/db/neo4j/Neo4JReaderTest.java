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
		printAllTerms();
		writer.addSentence(List.of("a", "b", "c"));
		writer.addSentence(List.of("b", "d", "c", "e"));
		writer.addDocument(Path.of("2"));
		writer.addSentence(List.of("b", "a"));
		writer.addSentence(List.of("a", "f"));

		printAllTerms();
		final var frequencies = reader.getTermFrequencies();
		var doc = frequencies.get("1");
		Assertions.assertEquals(1, doc.get("a"));
		Assertions.assertEquals(2, doc.get("b"));
		Assertions.assertEquals(2, doc.get("c"));
		Assertions.assertEquals(1, doc.get("d"));
		Assertions.assertEquals(1, doc.get("e"));
		doc = frequencies.get("2");
		Assertions.assertEquals(2, doc.get("a"));
		Assertions.assertEquals(1, doc.get("b"));
		Assertions.assertEquals(1, doc.get("f"));
	}

	@Test
	void getAllSentencesInDocument() {
		writer.addSentence(List.of("a", "b", "c", "d"));
		writer.addSentence(List.of("e", "f", "g", "h"));
	}
}

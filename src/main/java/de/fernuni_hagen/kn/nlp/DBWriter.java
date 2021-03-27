package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Sentence;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes to the database.
 *
 * @author Nils Wende
 */
public interface DBWriter {

	/**
	 * Removes all data from the database.
	 */
	void deleteAll();

	/**
	 * Adds a document to the DB.<br>
	 * No two input documents should have the same file name.
	 *
	 * @param path the document's file path
	 */
	void addDocument(Path path);

	/**
	 * Adds a sentence to the DB.
	 *
	 * @param terms a sentence in the form of terms
	 */
	void addSentence(List<String> terms);

	/**
	 * Adds a sentence to the DB.
	 *
	 * @param sentence a sentence
	 */
	default void addSentence(final Sentence sentence) {
		final var terms = sentence.getContent().collect(Collectors.toList());
		addSentence(terms);
	}

}

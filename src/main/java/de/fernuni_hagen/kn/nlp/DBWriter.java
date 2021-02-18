package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.preprocessing.Sentence;

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
	 * No two input documents can have the same file name.
	 *
	 * @param document the document
	 */
	void addDocument(Document document);

	/**
	 * Adds a sentence to the DB.
	 *
	 * @param distinctTerms a sentence in the form of distinct terms
	 */
	void addSentence(List<String> distinctTerms);

	/**
	 * Adds a sentence to the DB.
	 *
	 * @param sentence a sentence
	 */
	default void addSentence(final Sentence sentence) {
		final var distinctTerms = sentence.getContent().distinct().collect(Collectors.toList());
		addSentence(distinctTerms);
	}

}

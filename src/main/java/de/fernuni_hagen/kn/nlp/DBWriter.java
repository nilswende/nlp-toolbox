package de.fernuni_hagen.kn.nlp;

import java.util.List;

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
	 * No two input documents should have the same name.
	 *
	 * @param name the document's name
	 */
	void addDocument(String name);

	/**
	 * Adds a sentence to the DB.
	 *
	 * @param terms a sentence in the form of terms
	 */
	void addSentence(List<String> terms);

}

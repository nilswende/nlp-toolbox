package de.fernuni_hagen.kn.nlp;

import java.nio.file.Path;
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
	 * Adds a document to the DB.
	 *
	 * @param path the document's file pah
	 */
	void addDocument(Path path);

	/**
	 * Adds a sentence in the form of terms to the DB.
	 *
	 * @param terms terms of a sentence
	 */
	void addSentence(List<String> terms);

}

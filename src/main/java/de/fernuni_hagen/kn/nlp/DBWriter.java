package de.fernuni_hagen.kn.nlp;

import java.io.File;
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
	 * @param file the file containing the document
	 */
	void addDocument(File file);

	/**
	 * Adds a sentence in the form of terms to the DB.
	 *
	 * @param terms terms of a sentence
	 */
	void addSentence(List<String> terms);

}

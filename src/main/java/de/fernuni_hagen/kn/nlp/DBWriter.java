package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.preprocessing.Sentence;

import java.nio.file.Path;

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
	 * Adds a sentence to the DB.
	 *
	 * @param sentence a sentence
	 */
	void addSentence(Sentence sentence);

}

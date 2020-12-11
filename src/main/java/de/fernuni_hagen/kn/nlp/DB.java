package de.fernuni_hagen.kn.nlp;

import java.util.List;

/**
 * The graph database.
 *
 * @author Nils Wende
 */
public interface DB {

	/**
	 * Adds a sentence in the form of words to the DB.
	 *
	 * @param words words of a sentence
	 */
	void addSentence(List<String> words);

	/**
	 * Updates the Dice ratio and costs for all relationships present in the DB.
	 */
	void updateDiceAndCosts();

}

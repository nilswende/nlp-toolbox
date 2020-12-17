package de.fernuni_hagen.kn.nlp.workflow;

/**
 * Removes stop words from a sentence.
 *
 * @author Nils Wende
 */
public interface StopWordFilter {

	/**
	 * Removes stop words from a sentence.
	 *
	 * @param sentence the sentence
	 * @return the sentence without stop words
	 */
	String filter(String sentence);

}

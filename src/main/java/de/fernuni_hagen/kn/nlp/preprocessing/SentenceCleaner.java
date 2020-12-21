package de.fernuni_hagen.kn.nlp.preprocessing;

/**
 * Removes unwanted characters from a sentence.
 *
 * @author Nils Wende
 */
public interface SentenceCleaner {

	/**
	 * Removes unwanted characters from a sentence.
	 *
	 * @param sentence the sentence
	 * @return a sentence containing no unwanted characters
	 */
	String clean(CharSequence sentence);

}

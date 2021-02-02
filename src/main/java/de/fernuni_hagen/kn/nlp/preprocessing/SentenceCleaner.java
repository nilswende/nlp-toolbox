package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.function.Function;

/**
 * Removes unwanted characters from a sentence.
 *
 * @author Nils Wende
 */
public interface SentenceCleaner extends Function<CharSequence, String> {

	/**
	 * Removes unwanted characters from a sentence.
	 *
	 * @param sentence the sentence
	 * @return a sentence containing no unwanted characters
	 */
	String apply(CharSequence sentence);

}

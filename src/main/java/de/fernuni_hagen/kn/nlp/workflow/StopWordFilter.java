package de.fernuni_hagen.kn.nlp.workflow;

import java.util.stream.Stream;

/**
 * Removes stop words from a sentence.
 *
 * @author Nils Wende
 */
@FunctionalInterface
public interface StopWordFilter {

	/**
	 * Removes stop words from a sentence.
	 *
	 * @param sentence the words of a sentence
	 * @return the sentence without stop words
	 */
	Stream<String> filter(Stream<String> sentence);

}

package de.fernuni_hagen.kn.nlp.workflow;

import java.util.stream.Stream;

/**
 * Removes stop words from a sentence.
 *
 * @author Nils Wende
 */
public interface StopWordFilter {

	/**
	 * Removes stop words from a sentence.
	 *
	 * @param sentence the words of a sentence
	 * @return the sentence without stop words
	 */
	Stream<TaggedWord> filter(Stream<TaggedWord> sentence);

}

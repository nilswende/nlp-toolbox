package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.stream.Stream;

/**
 * Removes stop words from a sentence.
 *
 * @author Nils Wende
 */
public interface StopWordFilter extends PreprocessingStep {

	/**
	 * Removes stop words from a sentence.
	 *
	 * @param sentence the words of a sentence
	 * @return the sentence without stop words
	 */
	@Override
	Stream<TaggedWord> apply(Stream<TaggedWord> sentence);

}

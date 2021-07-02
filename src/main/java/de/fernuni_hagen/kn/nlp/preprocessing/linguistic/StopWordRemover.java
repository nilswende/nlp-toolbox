package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.data.TaggedTerm;

import java.util.stream.Stream;

/**
 * Removes stop words from a sentence.
 *
 * @author Nils Wende
 */
public interface StopWordRemover extends PreprocessingStep {

	/**
	 * Removes stop words from a sentence.
	 *
	 * @param sentence the words of a sentence
	 * @return the sentence without stop words
	 */
	@Override
	Stream<TaggedTerm> apply(Stream<TaggedTerm> sentence);

}

package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.stream.Stream;

/**
 * Removes abbreviations from a sentence.
 *
 * @author Nils Wende
 */
public interface AbbreviationRemover extends PreprocessingStep {

	/**
	 * Removes abbreviations from a sentence.
	 *
	 * @param sentence the words of a sentence
	 * @return the sentence without abbreviations
	 */
	@Override
	Stream<TaggedTerm> apply(Stream<TaggedTerm> sentence);

}

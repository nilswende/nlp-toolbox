package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.stream.Stream;

/**
 * Retains only nouns in sentences.
 *
 * @author Nils Wende
 */
public interface NounFilter extends PreprocessingStep {

	/**
	 * Retains only nouns in sentences.
	 *
	 * @param sentence the words of a sentence
	 * @return the sentence containing only nouns
	 */
	@Override
	Stream<TaggedTerm> apply(Stream<TaggedTerm> sentence);

}

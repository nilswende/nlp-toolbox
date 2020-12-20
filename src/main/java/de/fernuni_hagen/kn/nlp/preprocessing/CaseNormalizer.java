package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.stream.Stream;

/**
 * Normalizes the case of a word.
 *
 * @author Nils Wende
 */
public interface CaseNormalizer extends PreprocessingStep {

	/**
	 * Normalizes the case of a word.
	 *
	 * @param sentence the terms of a sentence
	 * @return the case-normalized terms
	 */
	@Override
	Stream<TaggedWord> apply(Stream<TaggedWord> sentence);

}

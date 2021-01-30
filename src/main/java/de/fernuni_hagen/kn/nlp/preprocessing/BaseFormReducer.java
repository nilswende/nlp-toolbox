package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.stream.Stream;

/**
 * Reduces the terms of a sentence to their base forms.
 *
 * @author Nils Wende
 */
public interface BaseFormReducer extends PreprocessingStep {

	/**
	 * Reduces a term to its base form.
	 *
	 * @param sentence the terms of a sentence
	 * @return the reduced terms
	 */
	@Override
	Stream<TaggedTerm> apply(Stream<TaggedTerm> sentence);

}

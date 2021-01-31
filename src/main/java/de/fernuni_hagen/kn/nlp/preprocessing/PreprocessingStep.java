package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A variable step in the preprocessing workflow.
 *
 * @author Nils Wende
 */
interface PreprocessingStep extends Function<Stream<TaggedTerm>, Stream<TaggedTerm>> {

	/**
	 * Applies the preprocessing step to the given sentence.
	 *
	 * @param sentence the words of a sentence
	 * @return the sentence with this preprocessing step applied
	 */
	@Override
	Stream<TaggedTerm> apply(Stream<TaggedTerm> sentence);

}

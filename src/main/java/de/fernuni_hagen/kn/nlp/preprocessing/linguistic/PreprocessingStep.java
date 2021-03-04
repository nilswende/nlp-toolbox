package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A variable step in the preprocessing workflow.
 *
 * @author Nils Wende
 */
public interface PreprocessingStep extends UnaryOperator<Stream<TaggedTerm>> {

	/**
	 * Applies the preprocessing step to the given sentence.
	 *
	 * @param sentence the words of a sentence
	 * @return the sentence with this preprocessing step applied
	 */
	@Override
	Stream<TaggedTerm> apply(Stream<TaggedTerm> sentence);

	/**
	 * Returns a composed PreprocessingStep that first applies this PreprocessingStep to
	 * its input, and then applies the {@code after} PreprocessingStep to the result.
	 * If evaluation of either PreprocessingStep throws an exception, it is relayed to
	 * the caller of the composed PreprocessingStep.
	 *
	 * @param after the step to apply after this step is applied
	 * @return a composed PreprocessingStep that first applies this step and then
	 * applies the {@code after} step
	 * @throws NullPointerException if after is null
	 * @see #andThen(Function)
	 */
	default PreprocessingStep chain(final PreprocessingStep after) {
		Objects.requireNonNull(after);
		return t -> after.apply(apply(t));
	}

}

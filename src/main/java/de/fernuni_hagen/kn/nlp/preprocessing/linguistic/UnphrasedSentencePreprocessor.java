package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.factory.PreprocessingFactory;

import java.util.List;
import java.util.function.Function;

/**
 * Executes the linguistic preprocessing on Strings, including phrase removal.
 *
 * @author Nils Wende
 */
public class UnphrasedSentencePreprocessor extends PhrasedSentencePreprocessor {

	UnphrasedSentencePreprocessor(final List<Function<PreprocessingFactory, PreprocessingStep>> preprocessingSteps, final PreprocessingFactory factory) {
		super(preprocessingSteps, factory);
	}

	@Override
	protected Sentence createSentence(final List<TaggedTerm> taggedTerms, final String sentence, final List<String> extractedPhrases) {
		return new Sentence(taggedTerms);
	}
}

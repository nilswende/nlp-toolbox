package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.preprocessing.factory.PreprocessingFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing of a document, including phrase extraction.
 *
 * @author Nils Wende
 */
class PhrasePreprocessor extends Preprocessor {

	PhrasePreprocessor(final List<Function<PreprocessingFactory, PreprocessingStep>> workflowSteps) {
		super(workflowSteps);
	}

	@Override
	protected Stream<Sentence> createSentences(final Stream<String> sentences, final PreprocessingFactory factory) {
		final var phraseExtractor = factory.createPhraseExtractor();
		final var tagger = factory.createTagger();
		return phraseExtractor
				.extractPhrases(sentences)
				.map(p -> new Sentence(tagger.apply(p.getLeft()), p.getRight()));
	}

}

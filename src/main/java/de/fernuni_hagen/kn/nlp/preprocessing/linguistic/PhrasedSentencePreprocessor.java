package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.factory.PreprocessingFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing on Strings, including phrase extraction.
 *
 * @author Nils Wende
 */
class PhrasedSentencePreprocessor extends SentencePreprocessor {

	PhrasedSentencePreprocessor(final List<Function<PreprocessingFactory, PreprocessingStep>> preprocessingSteps, final PreprocessingFactory factory) {
		super(preprocessingSteps, factory);
	}

	@Override
	protected Stream<Sentence> createSentences(final Stream<String> sentences) {
		final var phraseExtractor = factory.createPhraseExtractor();
		final var tagger = factory.createTagger();
		final var pair = phraseExtractor.extractPhrases(sentences);
		final var phrases = pair.getRight();
		return pair.getLeft()
				.map(s -> extract(tagger, s, phrases));
	}

	private PhrasedSentence extract(final Tagger tagger, final String sentence, final List<String> phrases) {
		final var phraseIterator = new PhraseIterator(sentence, phrases);
		final var extractedPhrases = phraseIterator.removeAll();
		return new PhrasedSentence(tagger.apply(phraseIterator.getSentence()), sentence, extractedPhrases);
	}

}

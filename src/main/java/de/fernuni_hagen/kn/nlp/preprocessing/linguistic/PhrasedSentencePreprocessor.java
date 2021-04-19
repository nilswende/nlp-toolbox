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
		final var phraseRecognizer = factory.createPhraseRecognizer();
		final var tagger = factory.createTagger();
		final var pair = phraseRecognizer.recognizePhrases(sentences);
		final var phrases = pair.getRight();
		return pair.getLeft()
				.map(s -> extract(tagger, s, phrases));
	}

	private Sentence extract(final Tagger tagger, final String sentence, final List<String> phrases) {
		final var phraseIterator = new PhraseIterator(sentence, phrases);
		final var extractedPhrases = phraseIterator.removeAll();
		final var taggedTerms = tagger.apply(phraseIterator.getSentence());
		return createSentence(taggedTerms, sentence, extractedPhrases);
	}

	/**
	 * Create a {@link Sentence} instance from the sentence string.
	 *
	 * @param taggedTerms      tagged terms
	 * @param sentence         sentence string
	 * @param extractedPhrases extracted phrases
	 * @return a {@link Sentence} instance
	 */
	protected Sentence createSentence(final List<TaggedTerm> taggedTerms, final String sentence, final List<String> extractedPhrases) {
		return new PhrasedSentence(taggedTerms, sentence, extractedPhrases);
	}

}

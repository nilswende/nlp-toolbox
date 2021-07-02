package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.phrases;

import de.fernuni_hagen.kn.nlp.preprocessing.FileSaver;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.PreprocessingStep;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.SentencePreprocessor;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Tagger;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.data.PhrasedSentence;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.data.Sentence;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.data.TaggedTerm;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.factory.PreprocessingFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing on Strings, including phrase handling.
 *
 * @author Nils Wende
 */
public class PhrasedSentencePreprocessor extends SentencePreprocessor {

	private final FileSaver fileSaver = new FileSaver("data/output/phrases.txt", true);
	private final boolean detectPhrases;
	private final boolean removePhrases;
	private final boolean extractPhrases;
	private List<String> phrases;

	/**
	 * Constructor.
	 *
	 * @param detectPhrases      detectPhrases
	 * @param removePhrases      removePhrases
	 * @param extractPhrases     extractPhrases
	 * @param preprocessingSteps PreprocessingSteps
	 * @param factory            PreprocessingFactory
	 */
	public PhrasedSentencePreprocessor(final boolean detectPhrases, final boolean removePhrases, final boolean extractPhrases, final List<Function<PreprocessingFactory, PreprocessingStep>> preprocessingSteps, final PreprocessingFactory factory) {
		super(preprocessingSteps, factory);
		this.detectPhrases = detectPhrases;
		this.removePhrases = removePhrases;
		this.extractPhrases = extractPhrases;
	}

	@Override
	protected Stream<Sentence> createSentences(final Stream<String> sentences) {
		final var phraseDetector = factory.createPhraseDetector();
		final var tagger = factory.createTagger();
		final var sentenceList = sentences.collect(Collectors.toList());
		phrases = phraseDetector.detectPhrases(sentenceList);
		phrases.forEach(fileSaver::println);
		return detectPhrases
				? super.createSentences(sentenceList.stream())
				: sentenceList.stream()
				.map(s -> extract(tagger, s));
	}

	private Sentence extract(final Tagger tagger, final String sentence) {
		final var phraseIterator = new PhraseIterator(sentence, phrases);
		final var extractedPhrases = phraseIterator.removeAll();
		final var iteratorSentence = phraseIterator.getSentence();
		if (iteratorSentence.isEmpty()) {
			return createSentence(List.of(), sentence, extractedPhrases);
		}
		final var taggedTerms = tagger.apply(iteratorSentence);
		return createSentence(taggedTerms, sentence, extractedPhrases);
	}

	private Sentence createSentence(final List<TaggedTerm> taggedTerms, final String sentence, final List<String> extractedPhrases) {
		if (removePhrases) {
			return new Sentence(taggedTerms);
		}
		if (extractPhrases) {
			return new PhrasedSentence(taggedTerms, sentence, extractedPhrases);
		}
		throw new IllegalArgumentException();
	}

	@Override
	public List<String> getPhrases() {
		return phrases;
	}

}

package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.factory.PreprocessingFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing of sentences.
 *
 * @author Nils Wende
 */
public class SentencePreprocessor {

	/**
	 * The preprocessing factory.
	 */
	protected final PreprocessingFactory factory;
	private final List<Function<PreprocessingFactory, PreprocessingStep>> preprocessingSteps;

	SentencePreprocessor(final List<Function<PreprocessingFactory, PreprocessingStep>> preprocessingSteps, final PreprocessingFactory factory) {
		this.preprocessingSteps = preprocessingSteps;
		this.factory = factory;
	}

	/**
	 * Executes the linguistic preprocessing of the given sentences.
	 *
	 * @param sentences DB
	 * @return the preprocessed sentences
	 */
	public Stream<Sentence> processSentences(final Stream<String> sentences) {
		final var cleanedSentences = cleanSentences(sentences);
		final var taggedSentences = createSentences(cleanedSentences);
		return applyPreprocessingSteps(taggedSentences);
	}

	private Stream<String> cleanSentences(final Stream<String> sentences) {
		return sentences
				.map(factory.createSentenceCleaner())
				.filter(s -> !s.isEmpty());
	}

	/**
	 * Create {@link Sentence} instances from the sentence strings.
	 *
	 * @param sentences sentence strings
	 * @return {@link Sentence} instances
	 */
	protected Stream<Sentence> createSentences(final Stream<String> sentences) {
		return sentences
				.map(factory.createTagger())
				.map(Sentence::new);
	}

	private Stream<Sentence> applyPreprocessingSteps(final Stream<Sentence> sentences) {
		return chainPreprocessingSteps()
				.map(steps -> sentences.map(s -> s.withTerms(steps)))
				.orElse(sentences);
	}

	private Optional<PreprocessingStep> chainPreprocessingSteps() {
		return preprocessingSteps.stream()
				.map(step -> step.apply(factory))
				.reduce(PreprocessingStep::chain);
	}

	/**
	 * Creates a linguistic preprocessor.
	 *
	 * @param detectPhrases      true if phrases should be detected
	 * @param removePhrases      true if phrases should be removed
	 * @param extractPhrases     true if phrases should be extracted
	 * @param preprocessingSteps preprocessing steps
	 * @param factory            PreprocessingFactory
	 * @return a linguistic preprocessor
	 */
	public static SentencePreprocessor from(final boolean detectPhrases, final boolean removePhrases, final boolean extractPhrases, final List<Function<PreprocessingFactory, PreprocessingStep>> preprocessingSteps, final PreprocessingFactory factory) {
		if (detectPhrases || removePhrases || extractPhrases) {
			return new PhrasedSentencePreprocessor(detectPhrases, removePhrases, extractPhrases, preprocessingSteps, factory);
		}
		return new SentencePreprocessor(preprocessingSteps, factory);
	}

	/**
	 * Returns the list of distinct phrases in the text.<br>
	 * This method returns null, if no form of phrase processing was enabled.
	 *
	 * @return the list of distinct phrases in the text or null
	 */
	public List<String> getPhrases() {
		return null;
	}

}

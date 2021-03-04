package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import de.fernuni_hagen.kn.nlp.preprocessing.Preprocessor;
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
	 * @param config  Preprocessor.Config
	 * @param factory PreprocessingFactory
	 * @return a linguistic preprocessor
	 */
	public static SentencePreprocessor from(final Preprocessor.Config config, final PreprocessingFactory factory) {
		return config.extractPhrases() ? new PhrasedSentencePreprocessor(config.getPreprocessingSteps(), factory)
				: new SentencePreprocessor(config.getPreprocessingSteps(), factory);
	}

}

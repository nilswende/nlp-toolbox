package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.preprocessing.factory.PreprocessingFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing on Strings.
 *
 * @author Nils Wende
 */
class SentencePreprocessor {

	protected final PreprocessingFactory factory;
	private final List<Function<PreprocessingFactory, PreprocessingStep>> preprocessingSteps;

	public SentencePreprocessor(final List<Function<PreprocessingFactory, PreprocessingStep>> preprocessingSteps, final PreprocessingFactory factory) {
		this.preprocessingSteps = preprocessingSteps;
		this.factory = factory;
	}

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

}

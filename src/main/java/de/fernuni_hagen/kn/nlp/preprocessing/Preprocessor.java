package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.preprocessing.factory.PreprocessingFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing of a document.
 *
 * @author Nils Wende
 */
public class Preprocessor {

	private final List<Function<PreprocessingFactory, PreprocessingStep>> preprocessingSteps;

	protected Preprocessor(final List<Function<PreprocessingFactory, PreprocessingStep>> preprocessingSteps) {
		this.preprocessingSteps = preprocessingSteps;
	}

	/**
	 * Executes the linguistic preprocessing of a document.
	 *
	 * @param document the document to be processed
	 * @return stream of the sentences inside the document, split into words
	 */
	public Stream<Sentence> preprocess(final Path document) {
		final var factory = PreprocessingFactory.from(document);
		final var sentenceExtractor = factory.createSentenceExtractor();
		final var sentences = sentenceExtractor.extract(document);
		final var cleanedSentences = cleanSentences(sentences, factory);
		return processSentences(cleanedSentences, factory);
	}

	private Stream<String> cleanSentences(final Stream<String> sentences, final PreprocessingFactory factory) {
		final var sentenceCleaner = factory.createSentenceCleaner();
		return sentences
				.map(sentenceCleaner::clean)
				.filter(s -> !s.isEmpty());
	}

	private Stream<Sentence> processSentences(final Stream<String> sentences, final PreprocessingFactory factory) {
		final var taggedSentences = createSentences(sentences, factory);
		return applyPreprocessingSteps(taggedSentences, factory);
	}

	protected Stream<Sentence> createSentences(final Stream<String> sentences, final PreprocessingFactory factory) {
		final var tagger = factory.createTagger();
		return sentences.map(s -> new Sentence(tagger.tag(s)));
	}

	private Stream<Sentence> applyPreprocessingSteps(final Stream<Sentence> taggedSentences, final PreprocessingFactory factory) {
		return preprocessingSteps.stream()
				.map(step -> (Function<Stream<TaggedTerm>, Stream<TaggedTerm>>) step.apply(factory))
				.reduce(Function::andThen)
				.map(steps -> taggedSentences.map(s -> s.withTerms(steps)))
				.orElse(taggedSentences);
	}

	/**
	 * Creates a new preprocessor from the given config.
	 *
	 * @param config Config
	 * @return a new preprocessor
	 */
	public static Preprocessor from(final Config config) {
		final var steps = new ArrayList<Function<PreprocessingFactory, PreprocessingStep>>();
		if (config.filterNouns()) {
			steps.add(PreprocessingFactory::createNounFilter);
		}
		if (config.removeAbbreviations()) {
			steps.add(PreprocessingFactory::createAbbreviationFilter);
		}
		if (config.removeStopWords()) {
			steps.add(PreprocessingFactory::createStopWordFilter);
		}
		if (config.normalizeCase()) {
			steps.add(PreprocessingFactory::createCaseNormalizer);
		}
		if (config.useBaseFormReduction()) {
			steps.add(PreprocessingFactory::createBaseFormReducer);
		}
		return config.extractPhrases() ? new PhrasePreprocessor(steps) : new Preprocessor(steps);
	}

}

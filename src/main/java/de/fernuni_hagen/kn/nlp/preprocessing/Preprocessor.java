package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.preprocessing.factory.PreprocessingFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
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
	public Stream<List<String>> preprocess(final File document) {
		final PreprocessingFactory factory = PreprocessingFactory.from(document);
		final SentenceExtractor sentenceExtractor = factory.createSentenceExtractor();
		final var sentences = sentenceExtractor.extract(document);
		return processSentences(sentences, factory);
	}

	protected Stream<List<String>> processSentences(final Stream<String> sentences, final PreprocessingFactory factory) {
		final var cleanedSentences = cleanSentences(sentences, factory);
		final var taggedSentences = tagSentences(cleanedSentences, factory);
		return applyPreprocessingSteps(taggedSentences, factory)
				.map(s -> s.map(TaggedWord::getTerm))
				.map(s -> s.collect(Collectors.toList()));
	}

	private Stream<String> cleanSentences(final Stream<String> sentences, final PreprocessingFactory factory) {
		final var sentenceCleaner = factory.createSentenceCleaner();
		return sentences
				.map(sentenceCleaner::clean)
				.filter(s -> !s.isEmpty());
	}

	private Stream<Stream<TaggedWord>> tagSentences(final Stream<String> sentences, final PreprocessingFactory factory) {
		final Tagger tagger = factory.createTagger();
		return sentences.map(tagger::tag);
	}

	private Stream<Stream<TaggedWord>> applyPreprocessingSteps(final Stream<Stream<TaggedWord>> taggedSentences, final PreprocessingFactory factory) {
		var stream = taggedSentences;
		final var steps = preprocessingSteps.stream().map(step -> step.apply(factory)).collect(Collectors.toList());
		for (final PreprocessingStep step : steps) {
			stream = stream.map(step::apply);
		}
		return stream;
	}

	/**
	 * Creates a new preprocessor from the given config.
	 *
	 * @param config Config
	 * @return a new preprocessor
	 */
	public static Preprocessor from(final Config config) {
		final var steps = new ArrayList<Function<PreprocessingFactory, PreprocessingStep>>();
		if (config.removeAbbreviations()) {
			steps.add(PreprocessingFactory::createAbbreviationFilter);
		}
		if (config.filterNouns()) {
			steps.add(PreprocessingFactory::createNounFilter);
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

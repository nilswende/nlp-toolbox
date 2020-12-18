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

	private final List<Function<PreprocessingFactory, PreprocessingStep>> workflowSteps;

	protected Preprocessor(final List<Function<PreprocessingFactory, PreprocessingStep>> workflowSteps) {
		this.workflowSteps = workflowSteps;
	}

	/**
	 * Executes the linguistic preprocessing of a document.
	 *
	 * @param document the document to be processed
	 * @return stream of the sentences inside the document, split into words
	 */
	public Stream<List<String>> preprocess(final File document) {
		final PreprocessingFactory factory = PreprocessingFactory.from(document);
		final var sentenceExtractor = factory.createSentenceExtractor();
		// file level
		final var sentences = sentenceExtractor.extract(document);
		return processSentences(sentences, factory);
	}

	protected Stream<List<String>> processSentences(final Stream<String> sentences, final PreprocessingFactory factory) {
		final var taggedSentences = createTaggedSentences(sentences, factory);
		return applyWorkflowSteps(taggedSentences, factory)
				.map(s -> s.map(TaggedWord::getTerm))
				.map(s -> s.collect(Collectors.toList()));
	}

	private Stream<Stream<TaggedWord>> createTaggedSentences(final Stream<String> sentences, final PreprocessingFactory factory) {
		final var tagger = factory.createTagger();
		return sentences.map(tagger::tag);
	}

	private Stream<Stream<TaggedWord>> applyWorkflowSteps(final Stream<Stream<TaggedWord>> taggedSentences, final PreprocessingFactory factory) {
		Stream<Stream<TaggedWord>> stream = taggedSentences;
		final var steps = workflowSteps.stream().map(step -> step.apply(factory)).collect(Collectors.toList());
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
		if (config.useBaseFormReduction()) {
			steps.add(PreprocessingFactory::createBaseFormReducer);
		}
		if (config.filterNouns()) {
			steps.add(PreprocessingFactory::createNounFilter);
		}
		if (config.removeStopWords()) {
			steps.add(PreprocessingFactory::createStopWordFilter);
		}
		if (config.removeAbbreviations()) {
			steps.add(PreprocessingFactory::createAbbreviationFilter);
		}
		return config.extractPhrases() ? new PhrasePreprocessor(steps) : new Preprocessor(steps);
	}

}

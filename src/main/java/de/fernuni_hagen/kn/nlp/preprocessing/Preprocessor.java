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
	private final boolean extractPhrases;

	private Preprocessor(final List<Function<PreprocessingFactory, PreprocessingStep>> preprocessingSteps, final boolean extractPhrases) {
		this.preprocessingSteps = preprocessingSteps;
		this.extractPhrases = extractPhrases;
	}

	/**
	 * Executes the linguistic preprocessing of a document.
	 *
	 * @param textFile the document to be processed
	 * @return stream of the sentences inside the document
	 */
	public Stream<Sentence> preprocess(final Path textFile) {
		final var factory = PreprocessingFactory.from(textFile);
		final var sentencePreprocessor = extractPhrases ? new PhrasedSentencePreprocessor(preprocessingSteps, factory)
				: new SentencePreprocessor(preprocessingSteps, factory);
		final var sentences = factory.createSentenceExtractor().extract(textFile);
		return sentencePreprocessor.processSentences(sentences);
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
			steps.add(PreprocessingFactory::createAbbreviationRemover);
		}
		if (config.removeStopWords()) {
			steps.add(PreprocessingFactory::createStopWordRemover);
		}
		if (config.normalizeCase()) {
			steps.add(PreprocessingFactory::createCaseNormalizer);
		}
		if (config.useBaseFormReduction()) {
			steps.add(PreprocessingFactory::createBaseFormReducer);
		}
		return new Preprocessor(steps, config.extractPhrases());
	}

}

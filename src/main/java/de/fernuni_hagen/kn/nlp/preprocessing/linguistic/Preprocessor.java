package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.Sentence;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.config.UseCaseConfig;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.factory.PreprocessingFactory;
import de.fernuni_hagen.kn.nlp.preprocessing.textual.TikaDocumentConverter;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.fernuni_hagen.kn.nlp.Logger.logDuration;
import static de.fernuni_hagen.kn.nlp.Logger.logStart;

/**
 * Executes the linguistic preprocessing of a document.
 *
 * @author Nils Wende
 */
public class Preprocessor extends UseCase {

	private final Config config;

	/**
	 * Creates a new preprocessor from the given config.
	 *
	 * @param config Preprocessor.Config
	 */
	public Preprocessor(final Config config) {
		this.config = config;
	}

	/**
	 * Preprocessor config.
	 */
	public static class Config extends UseCaseConfig {
		private String inputDir;
		private boolean keepTempFiles;
		private int sentenceFileSizeLimitBytes;
		private boolean extractPhrases;
		private boolean useBaseFormReduction;
		private boolean filterNouns;
		private boolean removeStopWords;
		private boolean removeAbbreviations;
		private boolean normalizeCase;

		public Path getInputDir() {
			return inputDir == null ? Path.of(AppConfig.DEFAULT_BASE_DIR, "input") : Path.of(inputDir);
		}

		public boolean keepTempFiles() {
			return keepTempFiles;
		}

		public int getSentenceFileSizeLimitBytes() {
			return sentenceFileSizeLimitBytes <= 0 ? Integer.MAX_VALUE : sentenceFileSizeLimitBytes;
		}

		public boolean extractPhrases() {
			return extractPhrases;
		}

		List<Function<PreprocessingFactory, PreprocessingStep>> getPreprocessingSteps() {
			final var steps = new ArrayList<Function<PreprocessingFactory, PreprocessingStep>>();
			if (filterNouns) {
				steps.add(PreprocessingFactory::createNounFilter);
			}
			if (removeAbbreviations) {
				steps.add(PreprocessingFactory::createAbbreviationRemover);
			}
			if (removeStopWords) {
				steps.add(PreprocessingFactory::createStopWordRemover);
			}
			if (normalizeCase) {
				steps.add(PreprocessingFactory::createCaseNormalizer);
			}
			if (useBaseFormReduction) {
				steps.add(PreprocessingFactory::createBaseFormReducer);
			}
			return steps;
		}
	}

	/**
	 * Executes the linguistic preprocessing of documents.
	 *
	 * @param db DB
	 */
	@Override
	public void execute(final DBWriter db) {
		final var start = logStart("preprocess");
		final var documentConverter = new TikaDocumentConverter(config.getSentenceFileSizeLimitBytes());
		try (final var paths = Files.walk(config.getInputDir())) {
			paths.filter(p -> Files.isRegularFile(p))
					.peek(db::addDocument)
					.map(documentConverter::convert)
					.flatMap(this::preprocess)
					.forEach(db::addSentence);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		} finally {
			if (!config.keepTempFiles()) {
				FileHelper.deleteTempFiles();
			}
			logDuration("preprocess", start);
		}
	}

	/**
	 * Executes the linguistic preprocessing of a document.
	 *
	 * @param textFile the document to be processed
	 * @return stream of the sentences inside the document
	 */
	private Stream<Sentence> preprocess(final Path textFile) {
		final var factory = PreprocessingFactory.from(textFile);
		final var preprocessingSteps = config.getPreprocessingSteps();
		final var sentencePreprocessor = config.extractPhrases() ? new PhrasedSentencePreprocessor(preprocessingSteps, factory)
				: new SentencePreprocessor(preprocessingSteps, factory);
		final var sentences = factory.createSentenceExtractor().extract(textFile);
		return sentencePreprocessor.processSentences(sentences);
	}

}

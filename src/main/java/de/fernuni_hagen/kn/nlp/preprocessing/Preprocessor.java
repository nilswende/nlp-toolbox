package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.PreprocessingStep;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Sentence;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.SentencePreprocessor;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.factory.PreprocessingFactory;
import de.fernuni_hagen.kn.nlp.preprocessing.textual.TikaDocumentConverter;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Executes the preprocessing of a document.
 *
 * @author Nils Wende
 */
public class Preprocessor extends UseCase {

	private InputStream input;
	private String documentName;
	private boolean keepTempFiles;
	private int sentenceFileSizeLimitBytes = Integer.MAX_VALUE;
	private boolean continueAfterReachingFileSizeLimit;
	private boolean extractPhrases;
	private boolean removePhrases;
	private boolean useBaseFormReduction;
	private boolean filterNouns;
	private boolean removeStopWords;
	private boolean removeAbbreviations;
	private boolean normalizeCase;

	private Result result;

	/**
	 * Preprocessor result.
	 */
	public static class Result extends UseCase.Result {
	}

	/**
	 * Executes the preprocessing of a document.
	 */
	@Override
	public void execute(final DBWriter dbWriter) {
		preprocess(dbWriter);
		result = new Result();
	}

	void preprocess(final DBWriter dbWriter) {
		final var documentConverter = new TikaDocumentConverter(sentenceFileSizeLimitBytes, continueAfterReachingFileSizeLimit);
		final var tempFile = documentConverter.convert(input, documentName);
		dbWriter.addDocument(documentName);
		preprocess(tempFile).forEach(dbWriter::addSentence);
		if (!keepTempFiles) {
			FileHelper.deleteFile(tempFile);
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
		final var sentences = factory.createSentenceExtractor().extract(textFile);
		return SentencePreprocessor.from(removePhrases, extractPhrases, getPreprocessingSteps(), factory).processSentences(sentences);
	}

	private List<Function<PreprocessingFactory, PreprocessingStep>> getPreprocessingSteps() {
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

	@Override
	public Result getResult() {
		return result;
	}

	public Preprocessor setInput(final InputStream input) {
		this.input = input;
		return this;
	}

	public Preprocessor setInput(final String input) {
		return setInput(new ReaderInputStream(new StringReader(input), AppConfig.DEFAULT_CHARSET));
	}

	public Preprocessor setDocumentName(final String documentName) {
		this.documentName = documentName;
		return this;
	}

	public Preprocessor setKeepTempFiles(final boolean keepTempFiles) {
		this.keepTempFiles = keepTempFiles;
		return this;
	}

	public Preprocessor setSentenceFileSizeLimitBytes(final int sentenceFileSizeLimitBytes) {
		this.sentenceFileSizeLimitBytes = sentenceFileSizeLimitBytes;
		return this;
	}

	public Preprocessor setContinueAfterReachingFileSizeLimit(final boolean continueAfterReachingFileSizeLimit) {
		this.continueAfterReachingFileSizeLimit = continueAfterReachingFileSizeLimit;
		return this;
	}

	public Preprocessor setExtractPhrases(final boolean extractPhrases) {
		this.extractPhrases = extractPhrases;
		return this;
	}

	public Preprocessor setRemovePhrases(final boolean removePhrases) {
		this.removePhrases = removePhrases;
		return this;
	}

	public Preprocessor setUseBaseFormReduction(final boolean useBaseFormReduction) {
		this.useBaseFormReduction = useBaseFormReduction;
		return this;
	}

	public Preprocessor setFilterNouns(final boolean filterNouns) {
		this.filterNouns = filterNouns;
		return this;
	}

	public Preprocessor setRemoveStopWords(final boolean removeStopWords) {
		this.removeStopWords = removeStopWords;
		return this;
	}

	public Preprocessor setRemoveAbbreviations(final boolean removeAbbreviations) {
		this.removeAbbreviations = removeAbbreviations;
		return this;
	}

	public Preprocessor setNormalizeCase(final boolean normalizeCase) {
		this.normalizeCase = normalizeCase;
		return this;
	}
}

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
 * Executes the preprocessing of a document and writes the processed terms into the database.
 *
 * @author Nils Wende
 */
public class Preprocessor extends UseCase {

	private InputStream input;
	private String documentName;
	private boolean keepTempFiles;
	private boolean saveSentenceFile;
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
	 * Creates a Preprocessor. The mandatory arguments must be set by other means.
	 */
	Preprocessor() {
	}

	/**
	 * Creates a Preprocessor with mandatory arguments.
	 *
	 * @param input        the input document to process
	 * @param documentName the input document name
	 */
	public Preprocessor(final String input, final String documentName) {
		this(new ReaderInputStream(new StringReader(input), AppConfig.DEFAULT_CHARSET), documentName);
	}

	/**
	 * Creates a Preprocessor with mandatory arguments.
	 *
	 * @param input        the input document to process
	 * @param documentName the input document name
	 */
	public Preprocessor(final InputStream input, final String documentName) {
		this.input = input;
		this.documentName = documentName;
	}

	/**
	 * Preprocessor result.
	 */
	public static class Result extends UseCase.Result {
		private final List<Path> tempFiles;

		Result(final List<Path> tempFiles) {
			this.tempFiles = tempFiles;
		}

		@Override
		protected void printResult() {
			printfCollection(tempFiles, "No temp files kept", "Temp file '%s'");
		}

		/**
		 * Returns the created temp files.
		 *
		 * @return the created temp files
		 */
		public List<Path> getTempFiles() {
			return tempFiles;
		}
	}

	/**
	 * Executes the preprocessing of a document.
	 */
	@Override
	public void execute(final DBWriter dbWriter) {
		result = preprocess(dbWriter);
	}

	Result preprocess(final DBWriter dbWriter) {
		final var tempFile = convertDocument();
		dbWriter.addDocument(documentName);
		final var sentenceFile = FileHelper.getTempFile(documentName + ".s");
		try (final var sentences = preprocess(tempFile, sentenceFile)) {
			sentences.map(Sentence::getContent).filter(c -> c.size() > 1).forEach(dbWriter::addSentence);
		}
		deleteTempFiles(tempFile, sentenceFile);
		return new Result(keepTempFiles ? (saveSentenceFile ? List.of(tempFile, sentenceFile) : List.of(tempFile)) : List.of());
	}

	private Path convertDocument() {
		final var documentConverter = new TikaDocumentConverter(sentenceFileSizeLimitBytes, continueAfterReachingFileSizeLimit);
		return documentConverter.convert(input, documentName);
	}

	/**
	 * Executes the linguistic preprocessing of a document.
	 *
	 * @param textFile     the document to be processed
	 * @param sentenceFile the sentence file for saving
	 * @return stream of the sentences inside the document
	 */
	private Stream<Sentence> preprocess(final Path textFile, final Path sentenceFile) {
		final var fileSaver = new FileSaver(sentenceFile, saveSentenceFile);
		final var factory = PreprocessingFactory.from(textFile);
		final var sentences = factory.createSentenceExtractor().extract(textFile).peek(fileSaver::println);
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

	private void deleteTempFiles(final Path... files) {
		if (!keepTempFiles) {
			FileHelper.deleteFiles(files);
		}
	}

	@Override
	public Result getResult() {
		return result;
	}

	/**
	 * Set the input document.
	 *
	 * @param input the input document
	 * @return this object
	 */
	Preprocessor setInput(final InputStream input) {
		this.input = input;
		return this;
	}

	/**
	 * Set the input document name.
	 *
	 * @param documentName the input document name
	 * @return this object
	 */
	Preprocessor setDocumentName(final String documentName) {
		this.documentName = documentName;
		return this;
	}

	/**
	 * Set true, if the temporary files should be kept (e. g. sentence files), false, if they should be deleted after preprocessing.
	 *
	 * @param keepTempFiles true, if the temporary files should be kept
	 * @return this object
	 */
	public Preprocessor setKeepTempFiles(final boolean keepTempFiles) {
		this.keepTempFiles = keepTempFiles;
		return this;
	}

	/**
	 * Set true, if the sentence file should be saved, false otherwise.
	 *
	 * @param saveSentenceFile true, if the sentence file should be saved
	 * @return this object
	 */
	public Preprocessor setSaveSentenceFile(final boolean saveSentenceFile) {
		this.saveSentenceFile = saveSentenceFile;
		return this;
	}

	/**
	 * Set the size limit for sentence files in bytes.
	 *
	 * @param sentenceFileSizeLimitBytes the size limit for sentence files in bytes
	 * @return this object
	 */
	public Preprocessor setSentenceFileSizeLimitBytes(final int sentenceFileSizeLimitBytes) {
		this.sentenceFileSizeLimitBytes = sentenceFileSizeLimitBytes;
		return this;
	}

	/**
	 * Set true, if processing the partial sentence file after reaching the size limit for sentence files should continue, false, if the processing should be aborted.
	 *
	 * @param continueAfterReachingFileSizeLimit true, if processing the partial sentence file after reaching the size limit for sentence files should continue
	 * @return this object
	 */
	public Preprocessor setContinueAfterReachingFileSizeLimit(final boolean continueAfterReachingFileSizeLimit) {
		this.continueAfterReachingFileSizeLimit = continueAfterReachingFileSizeLimit;
		return this;
	}

	/**
	 * Set true, if phrases should be extracted, false otherwise.
	 *
	 * @param extractPhrases true, if phrases should be extracted
	 * @return this object
	 */
	public Preprocessor setExtractPhrases(final boolean extractPhrases) {
		this.extractPhrases = extractPhrases;
		return this;
	}

	/**
	 * Set true, if phrases should be removed, false otherwise.
	 *
	 * @param removePhrases true, if phrases should be removed
	 * @return this object
	 */
	public Preprocessor setRemovePhrases(final boolean removePhrases) {
		this.removePhrases = removePhrases;
		return this;
	}

	/**
	 * Set true, if base form reduction should be applied, false otherwise.
	 *
	 * @param useBaseFormReduction true, if base form reduction should be applied
	 * @return this object
	 */
	public Preprocessor setUseBaseFormReduction(final boolean useBaseFormReduction) {
		this.useBaseFormReduction = useBaseFormReduction;
		return this;
	}

	/**
	 * Set true, if only nouns should be kept, false otherwise.
	 *
	 * @param filterNouns true, if nouns should be filtered
	 * @return this object
	 */
	public Preprocessor setFilterNouns(final boolean filterNouns) {
		this.filterNouns = filterNouns;
		return this;
	}

	/**
	 * Set true, if stop words should be removed, false otherwise.
	 *
	 * @param removeStopWords true, if stop words should be removed
	 * @return this object
	 */
	public Preprocessor setRemoveStopWords(final boolean removeStopWords) {
		this.removeStopWords = removeStopWords;
		return this;
	}

	/**
	 * Set true, if abbreviations should be removed, false otherwise.
	 *
	 * @param removeAbbreviations true, if abbreviations should be removed
	 * @return this object
	 */
	public Preprocessor setRemoveAbbreviations(final boolean removeAbbreviations) {
		this.removeAbbreviations = removeAbbreviations;
		return this;
	}

	/**
	 * Set true, if case should be normalized, false otherwise.
	 *
	 * @param normalizeCase true, if case should be normalized
	 * @return this object
	 */
	public Preprocessor setNormalizeCase(final boolean normalizeCase) {
		this.normalizeCase = normalizeCase;
		return this;
	}
}

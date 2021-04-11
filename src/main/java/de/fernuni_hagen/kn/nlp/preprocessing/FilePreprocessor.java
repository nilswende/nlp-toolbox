package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Executes the preprocessing of file documents.
 *
 * @author Nils Wende
 */
public class FilePreprocessor extends Preprocessor {

	private Path inputDir;

	private Result result;

	/**
	 * FilePreprocessor result.
	 */
	public static class Result extends Preprocessor.Result {
		private final List<String> documentNames;

		public Result(final List<String> documentNames, final List<Path> tempFiles) {
			super(tempFiles);
			this.documentNames = documentNames;
		}

		@Override
		protected void printResult() {
			printfCollection(documentNames, "No documents found", "Processed '%s'");
			super.printResult();
		}

		/**
		 * Returns the names of the processed documents.
		 *
		 * @return the names of the processed documents
		 */
		public List<String> getDocumentNames() {
			return documentNames;
		}
	}

	/**
	 * Executes the preprocessing of file documents.
	 */
	@Override
	public void execute(final DBWriter dbWriter) {
		final var names = new ArrayList<String>();
		final var tempFiles = new ArrayList<Path>();
		for (final Path document : getDocuments()) {
			final var name = document.getFileName().toString();
			final var tempFile = preprocess(document, name, dbWriter);
			names.add(name);
			if (tempFile != null) {
				tempFiles.add(tempFile);
			}
		}
		result = new Result(names, tempFiles);
	}

	private List<Path> getDocuments() {
		final var inputDir = this.inputDir == null ? Path.of(appConfig.getWorkingDir(), "input") : this.inputDir;
		try (final var paths = Files.walk(inputDir)) {
			return paths.filter(p -> Files.isRegularFile(p))
					.collect(Collectors.toList());
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private Path preprocess(final Path path, final String name, final DBWriter dbWriter) {
		try (final var input = FileHelper.newBufferedInputStream(path)) {
			return setInput(input).setDocumentName(name).preprocess(dbWriter);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	@Override
	public Result getResult() {
		return result;
	}

	/**
	 * Set the input directory.
	 *
	 * @param inputDir the input directory
	 * @return this object
	 */
	public FilePreprocessor setInputDir(final String inputDir) {
		this.inputDir = Path.of(inputDir);
		return this;
	}
}

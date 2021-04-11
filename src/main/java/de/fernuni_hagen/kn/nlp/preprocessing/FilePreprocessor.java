package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Executes the preprocessing of file documents.
 *
 * @author Nils Wende
 */
public class FilePreprocessor extends Preprocessor {

	private String inputDir = Path.of(AppConfig.DEFAULT_BASE_DIR, "input").toString();

	private Result result;

	/**
	 * FilePreprocessor result.
	 */
	public static class Result extends Preprocessor.Result {
		private final List<String> documentNames;

		Result(final List<String> documentNames) {
			this.documentNames = documentNames;
		}

		@Override
		protected void printResult() {
			printfCollection(documentNames, "no documents found", "processed '%s'");
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
		try (final var paths = Files.walk(Path.of(inputDir))) {
			final var names = paths.filter(p -> Files.isRegularFile(p))
					.map(p -> preprocess(p, dbWriter))
					.collect(Collectors.toList());
			result = new Result(names);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private String preprocess(final Path path, final DBWriter dbWriter) {
		final var name = path.getFileName().toString();
		try (final var input = FileHelper.newBufferedInputStream(path)) {
			setInput(input).setDocumentName(name).preprocess(dbWriter);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
		return name;
	}

	@Override
	public Result getResult() {
		return result;
	}

	public FilePreprocessor setInputDir(final String inputDir) {
		this.inputDir = inputDir;
		return this;
	}
}

package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
	}

	/**
	 * Executes the preprocessing of file documents.
	 */
	@Override
	public void execute(final DBWriter dbWriter) {
		try (final var paths = Files.walk(Path.of(inputDir))) {
			paths.filter(p -> Files.isRegularFile(p))
					.forEach(p -> preprocess(p, dbWriter));
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
		result = new Result();
	}

	private void preprocess(final Path path, final DBWriter dbWriter) {
		try (final var input = FileHelper.newBufferedInputStream(path)) {
			super.setInput(input)
					.setDocumentName(path.getFileName().toString())
					.preprocess(dbWriter);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
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

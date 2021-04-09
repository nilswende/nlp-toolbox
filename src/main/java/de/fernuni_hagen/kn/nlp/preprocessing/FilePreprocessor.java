package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Executes the preprocessing of file documents.
 *
 * @author Nils Wende
 */
public class FilePreprocessor extends Preprocessor {

	private Path inputDir = Path.of(AppConfig.DEFAULT_BASE_DIR, "input");

	/**
	 * Executes the preprocessing of file documents.
	 */
	@Override
	public void execute(final DBWriter db) {
		try (final var paths = Files.walk(inputDir)) {
			paths.filter(p -> Files.isRegularFile(p))
					.peek(p -> super
							.setInput(newReader(p))
							.setDocumentName(p.getFileName().toString()))
					.forEach(p -> super.execute(db));
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private Reader newReader(final Path path) {
		try {
			return Files.newBufferedReader(path, AppConfig.DEFAULT_CHARSET);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	public Preprocessor setInputDir(final Path inputDir) {
		this.inputDir = inputDir;
		return this;
	}

}

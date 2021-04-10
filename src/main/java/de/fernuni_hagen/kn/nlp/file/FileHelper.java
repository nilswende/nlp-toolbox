package de.fernuni_hagen.kn.nlp.file;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Helps with file operations.
 *
 * @author Nils Wende
 */
public final class FileHelper {

	private FileHelper() {
		throw new AssertionError("no init");
	}

	/**
	 * Deletes a file.
	 *
	 * @param file the file to delete
	 */
	public static void deleteFile(final Path file) {
		try {
			Files.delete(file);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	/**
	 * Opens a file for streaming.
	 *
	 * @param path the file path
	 * @return InputStream
	 * @throws IOException if an I/O error occurs
	 */
	public static InputStream newBufferedInputStream(final Path path) throws IOException {
		return new BufferedInputStream(Files.newInputStream(path));
	}

	/**
	 * Opens a file for reading with the default charset.
	 *
	 * @param path the file path
	 * @return Reader
	 * @throws IOException if an I/O error occurs opening the file
	 */
	public static Reader newFileReader(final Path path) throws IOException {
		return Files.newBufferedReader(path, AppConfig.DEFAULT_CHARSET);
	}

}

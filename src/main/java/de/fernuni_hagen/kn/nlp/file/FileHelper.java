package de.fernuni_hagen.kn.nlp.file;

import de.fernuni_hagen.kn.nlp.config.AppConfig;

import java.io.IOException;
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

package de.fernuni_hagen.kn.nlp.file;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Helps with file operations.
 *
 * @author Nils Wende
 */
public final class FileHelper {

	private FileHelper() {
		// no init
	}

	/**
	 * Deletes files.
	 *
	 * @param files the files to delete
	 */
	public static void deleteFiles(final Path... files) {
		Arrays.stream(files).forEach(FileHelper::deleteFile);
	}

	/**
	 * Deletes a file.
	 *
	 * @param file the file to delete
	 */
	public static void deleteFile(final Path file) {
		try {
			if (Files.exists(file)) {
				Files.delete(file);
			}
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

	/**
	 * Opens or creates a file for writing with the default charset.
	 *
	 * @param path the file path
	 * @return OutputStream
	 * @throws IOException if an I/O error occurs
	 */
	public static OutputStream newBufferedOutputStream(final Path path) throws IOException {
		createParentDirectories(path);
		return new BufferedOutputStream(Files.newOutputStream(path));
	}

	/**
	 * Opens or creates a file for writing with the default charset.
	 *
	 * @param path the file path
	 * @return Writer
	 * @throws IOException if an I/O error occurs opening or creating the file
	 */
	public static Writer newBufferedWriter(final Path path) throws IOException {
		createParentDirectories(path);
		return Files.newBufferedWriter(path, AppConfig.DEFAULT_CHARSET);
	}

	/**
	 * Creates all nonexistent parent directories.
	 *
	 * @param path the file path
	 * @throws IOException if an I/O error occurs
	 */
	private static void createParentDirectories(final Path path) throws IOException {
		final var parent = path.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
	}

	/**
	 * Opens or creates a file for printing with the default charset.
	 *
	 * @param path the file path
	 * @return PrintWriter
	 * @throws UncheckedException if an I/O error occurs opening or creating the file
	 */
	public static PrintWriter newPrintWriter(final Path path) {
		try {
			return new PrintWriter(newBufferedWriter(path));
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	/**
	 * Returns the path to the temp directory.
	 *
	 * @return the path to the temp directory
	 */
	public static Path getTempDirectory() {
		return FileUtils.getTempDirectory().toPath();
	}

	/**
	 * Returns the path to the named temp file with the file extension {@code .txt}.
	 *
	 * @param name file name
	 * @return path to the temp file
	 */
	public static Path getTempFile(final String name) {
		final var tempDirectory = getTempDirectory();
		return Path.of(tempDirectory.resolve(name) + ".txt");
	}

}

package de.fernuni_hagen.kn.nlp.file;

import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helps with file operations.
 *
 * @author Nils Wende
 */
public final class FileHelper {

	private static final List<Path> tempFiles = Collections.synchronizedList(new ArrayList<>());

	private FileHelper() {
		throw new AssertionError(); // no init
	}

	public static Path createTempFile(final String suffix) {
		try {
			final var tempFile = Files.createTempFile("nlp", suffix);
			tempFiles.add(tempFile);
			return tempFile;
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	public static void delete(final Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	public static void deleteTempFiles() {
		synchronized (tempFiles) {
			tempFiles.forEach(FileHelper::delete);
			tempFiles.clear();
		}
	}

	public static Reader newFileReader(final Path path) throws IOException {
		return Files.newBufferedReader(path, Config.DEFAULT_CHARSET);
	}

}

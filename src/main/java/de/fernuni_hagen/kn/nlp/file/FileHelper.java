package de.fernuni_hagen.kn.nlp.file;

import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helps with file operations.
 *
 * @author Nils Wende
 */
public class FileHelper {

	private static final List<File> tempFiles = Collections.synchronizedList(new ArrayList<>());

	private FileHelper() {
		throw new AssertionError(); // no init
	}

	public static File createTempFile(final String suffix) {
		try {
			final var tempFile = File.createTempFile("nlp", suffix);
			tempFiles.add(tempFile);
			return tempFile;
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	public static void delete(final File file) {
		if (file != null && !file.delete()) {
			LoggerFactory.getLogger(FileHelper.class).warn("could not delete " + file);
		}
	}

	public static void deleteTempFiles() {
		tempFiles.forEach(FileHelper::delete);
	}

	public static Reader newFileReader(final File file) throws IOException {
		return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
	}

}

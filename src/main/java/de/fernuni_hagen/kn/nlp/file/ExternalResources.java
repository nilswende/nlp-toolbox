package de.fernuni_hagen.kn.nlp.file;

import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Copies JAR resources into temporary files for use by certain libraries.
 *
 * @author Nils Wende
 */
public class ExternalResources {

	/**
	 * Returns a file for the given JAR resource.
	 *
	 * @param fileName JAR resource
	 * @return temporary file path
	 */
	public static String getFile(final String fileName) {
		try {
			final File tempFile = getTempFile(fileName);
			if (tempFile.createNewFile()) {
				writeFile(fileName, tempFile);
			}
			return tempFile.getPath();
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private static File getTempFile(final String fileName) throws IOException {
		final var tempDir = Path.of(FileUtils.getTempDirectoryPath()).resolve("nlp-toolbox");
		Files.createDirectories(tempDir);
		final var tempFileName = fileName.replace(File.separatorChar, '_');
		return tempDir.resolve(tempFileName).toFile();
	}

	private static void writeFile(final String fileName, final File file) throws IOException {
		final var inputStream = Objects.requireNonNull(ExternalResources.class.getClassLoader().getResourceAsStream(fileName));
		FileUtils.copyInputStreamToFile(inputStream, file);
	}

}

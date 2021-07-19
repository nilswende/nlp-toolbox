package de.fernuni_hagen.kn.nlp.preprocessing.textual.impl;

import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Input utilities.
 *
 * @author Nils Wende
 */
public final class InputUtils {

	private InputUtils() {
		// no init
	}

	/**
	 * Counts the number of characters in the file using the given charset.
	 *
	 * @param path    the file to be read
	 * @param charset the file's charset
	 * @return the number of characters in the file
	 */
	public static long countChars(final Path path, final Charset charset) {
		try (final Reader reader = Files.newBufferedReader(path, charset)) {
			return countChars(reader);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	/**
	 * Counts the number of characters in the Reader.
	 *
	 * @param reader Reader
	 * @return the number of characters in the file
	 */
	public static long countChars(final Reader reader) {
		try {
			long count = 0;
			final char[] chars = new char[IOUtils.DEFAULT_BUFFER_SIZE];
			for (int read; (read = reader.read(chars)) != IOUtils.EOF; ) {
				count += read;
			}
			return count;
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

}

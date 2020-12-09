package de.fernuni_hagen.kn.nlp.text;

import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Reads characters from a file.
 *
 * @author Nils Wende
 */
public class BufferedFileReader implements Closeable {

	private final File file;
	private final Charset charset;
	private Reader reader;
	private long offset;

	/**
	 * Constructor.
	 *
	 * @param file    the file to be read
	 * @param charset the file's charset
	 */
	public BufferedFileReader(final File file, final Charset charset) {
		this.file = file;
		this.charset = charset;
	}

	/**
	 * Reads a character from the position {@code pos}.
	 *
	 * @param pos the position to be read from
	 * @return the character read or -1, if the file ended
	 * @throws UncheckedException if the file cannot be read
	 */
	public int read(final long pos) {
		try {
			ensureReader(pos);
			final var read = getReader().read();
			offset++;
			return read;
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private void ensureReader(final long start) throws IOException {
		if (offset < start) {
			skipTo(start);
		} else if (offset > start) {
			resetReader();
			ensureReader(start);
		}
	}

	private void skipTo(final long start) throws IOException {
		final var toSkip = start - offset;
		final var skipped = getReader().skip(toSkip);
		offset += skipped;
	}

	private void resetReader() throws IOException {
		getReader().close();
		reader = null;
		offset = 0;
	}

	/**
	 * Reads {@code length} characters from the position {@code start}.
	 *
	 * @param start  the position to be read from
	 * @param length the number of characters to be read
	 * @return the {@code length} characters read or null, if the file ended
	 * @throws UncheckedException if the file cannot be read
	 */
	public char[] read(final long start, final int length) {
		if (start < 0 || length < 0) {
			throw new IllegalArgumentException();
		}
		try {
			return getChars(start, length);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private char[] getChars(final long start, final int length) throws IOException {
		ensureReader(start);
		return getChars(length);
	}

	private char[] getChars(final int length) throws IOException {
		final var chars = new char[length];
		final var read = IOUtils.read(getReader(), chars);
		if (read == -1) {
			return null;
		}
		offset += read;
		return read < length ? Arrays.copyOf(chars, read) : chars;
	}

	private Reader getReader() {
		return reader == null ? reader = createReader() : reader;
	}

	// non private for tests
	Reader createReader() {
		try {
			offset = 0;
			return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	/**
	 * Returns the length (number of chars) of the backing file.
	 *
	 * @return file length
	 */
	public long getLength() {
		return Utils.countChars(file, StandardCharsets.UTF_8);
	}

	@Override
	public String toString() {
		return "BufferedFileReader{" +
				"file=" + file +
				", offset=" + offset +
				'}';
	}

}

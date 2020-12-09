package de.fernuni_hagen.kn.nlp.text;

import org.apache.commons.lang3.NotImplementedException;

import java.io.Closeable;
import java.io.IOException;
import java.text.CharacterIterator;

public class BufferedFileCharacterIterator implements CharacterIterator, Closeable {

	private final BufferedFileReader reader;
	private final int start;
	private final int end;
	private int pos;

	/**
	 * Constructor.
	 *
	 * @param reader reads characters from a file
	 * @param end    position one past the last character to be read, must be within the reader's range
	 */
	public BufferedFileCharacterIterator(final BufferedFileReader reader, final int end) {
		this.reader = reader;
		start = 0;
		this.end = end;
		pos = 0;
	}

	@Override
	public char first() {
		return setIndexInternal(start);
	}

	@Override
	public char last() {
		return setIndexInternal(end == start ? end : end - 1);
	}

	@Override
	public char current() {
		if (start <= pos && pos < end) {
			final var read = reader.read(pos);
			return (char) read;
		} else {
			return DONE;
		}
	}

	@Override
	public char next() {
		if (pos + 1 < end) {
			return setIndexInternal(pos + 1);
		}
		return DONE;
	}

	@Override
	public char previous() {
		if (pos > start) {
			return setIndexInternal(pos - 1);
		}
		return DONE;
	}

	@Override
	public char setIndex(final int position) {
		if (position < start || position > end) {
			throw new IllegalArgumentException("Invalid index");
		}
		return setIndexInternal(position);
	}

	private char setIndexInternal(final int position) {
		pos = position;
		return current();
	}

	@Override
	public int getBeginIndex() {
		return start;
	}

	@Override
	public int getEndIndex() {
		return end;
	}

	@Override
	public int getIndex() {
		return pos;
	}

	@Override
	public Object clone() {
		throw new NotImplementedException("Not supported");
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

}

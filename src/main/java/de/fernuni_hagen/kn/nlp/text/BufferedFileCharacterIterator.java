package de.fernuni_hagen.kn.nlp.text;

import org.apache.commons.lang3.NotImplementedException;

import java.io.Closeable;
import java.io.IOException;
import java.text.CharacterIterator;

/**
 * Adapts a BufferedFileReader to the CharacterIterator interface.
 *
 * @author Nils Wende
 */
public class BufferedFileCharacterIterator implements CharacterIterator, Closeable {

	private final BufferedFileReader reader;
	private final int start;
	private final int end;
	private int pos;
	// cache current
	private int currentPos = -1;
	private char current;

	/**
	 * Constructor.
	 *
	 * @param reader reads characters from a file
	 */
	public BufferedFileCharacterIterator(final BufferedFileReader reader) {
		this.reader = reader;
		start = 0;
		final var length = reader.getLength();
		if (length > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(String.format("File in reader %s is too big for the CharacterIterator interface", reader));
		}
		this.end = (int) length;
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
			if (currentPos != pos) {
				current = (char) reader.read(pos);
				currentPos = pos;
			}
			return current;
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

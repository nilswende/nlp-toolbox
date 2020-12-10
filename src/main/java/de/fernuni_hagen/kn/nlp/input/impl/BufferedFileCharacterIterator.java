package de.fernuni_hagen.kn.nlp.input.impl;

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

	private final CurrentCache currentCache;
	private final int start = 0;
	private final int end;
	private int pos = 0;

	/**
	 * Constructor.
	 *
	 * @param fileReader reads characters from a file
	 */
	public BufferedFileCharacterIterator(final BufferedFileReader fileReader) {
		this.currentCache = new CurrentCache(fileReader);
		final var length = fileReader.getLength();
		if (length > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(String.format("File in reader %s is too big for the CharacterIterator interface", fileReader));
		}
		this.end = (int) length;
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
			return currentCache.get(pos);
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
		currentCache.close();
	}

	/**
	 * Caches the current value for repeated calls.
	 */
	private static class CurrentCache implements Closeable {

		private final BufferedFileReader fileReader;
		private int currentPos = -1;
		private char current;

		CurrentCache(final BufferedFileReader fileReader) {
			this.fileReader = fileReader;
		}

		public char get(final int pos) {
			if (currentPos != pos) {
				current = (char) fileReader.read(pos);
				currentPos = pos;
			}
			return current;
		}

		@Override
		public void close() throws IOException {
			fileReader.close();
		}

	}

}

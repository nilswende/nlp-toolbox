package de.fernuni_hagen.kn.nlp.text;

import de.fernuni_hagen.kn.nlp.text.BufferedFileReaderTest.TestReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.CharacterIterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nils Wende
 */
class BufferedFileCharacterIteratorTest {

	@Test
	void traverseForward() throws IOException {
		final var s = "abc";
		try (final var reader = new TestReader(s);
			 final var iter = new BufferedFileCharacterIterator(reader, s.length())) {
			final var sb = new StringBuilder();
			for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
				sb.append(c);
			}
			assertEquals(s, sb.toString());
		}
	}

	@Test
	void traverseBackward() throws IOException {
		final var s = "abc";
		try (final var reader = new TestReader(s);
			 final var iter = new BufferedFileCharacterIterator(reader, s.length())) {
			final var sb = new StringBuilder();
			for (char c = iter.last(); c != CharacterIterator.DONE; c = iter.previous()) {
				sb.append(c);
			}
			assertEquals(s, sb.reverse().toString());
		}
	}

	@Test
	void traverseOut() throws IOException {
		final var s = "abcde";
		try (final var reader = new TestReader(s);
			 final var iter = new BufferedFileCharacterIterator(reader, s.length())) {
			final var sb = new StringBuilder();
			for (char c = iter.setIndex(2); c != CharacterIterator.DONE; c = iter.previous()) {
				sb.append(c);
			}
			for (char c = iter.setIndex(2); c != CharacterIterator.DONE; c = iter.next()) {
				sb.append(c);
			}
			assertEquals("cbacde", sb.toString());
		}
	}

	@Test
	void endTooLong() throws IOException {
		final var s = "abc";
		try (final var reader = new TestReader(s);
			 final var iterator = new BufferedFileCharacterIterator(reader, s.length() + 5)) {
			iterator.last();
			final var previous = iterator.previous();
			assertEquals('\uffff', previous);
		}
	}

}

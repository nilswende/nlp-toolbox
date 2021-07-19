package de.fernuni_hagen.kn.nlp.preprocessing.textual.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.textual.impl.BufferedFileReaderTest.TestReader;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.text.CharacterIterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Nils Wende
 */
class BufferedFileCharacterIteratorTest {

	@Test
	void traverseForward() throws IOException {
		final var s = "abc";
		try (final var reader = new TestReader(s);
			 final var iter = new BufferedFileCharacterIterator(reader)) {
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
			 final var iter = new BufferedFileCharacterIterator(reader)) {
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
			 final var iter = new BufferedFileCharacterIterator(reader)) {
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
	void repeatCurrent() throws IOException {
		final var s = "abc";
		try (final var reader = new TestReader(s);
			 final var iter = new BufferedFileCharacterIterator(reader)) {
			final var sb = new StringBuilder();
			for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
				for (int i = 0; i < 3; i++) {
					sb.append(iter.current());
				}
			}
			assertEquals("aaabbbccc", sb.toString());
		}
	}

	@Test
	void testExceptionConstructor() {
		final var mock = Mockito.mock(BufferedFileReader.class);
		Mockito.when(mock.getLength()).thenReturn(Long.MAX_VALUE);
		assertThrows(Exception.class, () -> new BufferedFileCharacterIterator(mock));
	}

	@Test
	void testExceptions() throws IOException {
		final var s = "abc";
		try (final var reader = new TestReader(s);
			 final var iter = new BufferedFileCharacterIterator(reader)) {
			final var beginIndex = iter.getBeginIndex();
			assertThrows(Exception.class, () -> iter.setIndex(beginIndex - 1));
			assertThrows(Exception.class, iter::clone);
		}
	}
}

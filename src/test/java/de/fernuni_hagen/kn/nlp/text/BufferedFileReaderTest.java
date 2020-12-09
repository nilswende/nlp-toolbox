package de.fernuni_hagen.kn.nlp.text;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class BufferedFileReaderTest {

	@ParameterizedTest
	@MethodSource
	void readCharOnce(final String s, final long pos, final int expected) throws IOException {
		try (final var reader = new TestReader(s)) {
			final var chars = reader.read(pos);
			assertEquals(expected, chars);
		}
	}

	static Stream<Arguments> readCharOnce() {
		return Stream.of(//
				arguments("abc", 0, 'a'),
				arguments("abc", 1, 'b'),
				arguments("abc", 2, 'c'),
				arguments("abc", 3, -1)
		);
	}

	@ParameterizedTest
	@MethodSource
	void readChars(final String s, final String expected, final long... pos) throws IOException {
		try (final var reader = new TestReader(s)) {
			final var actual = Arrays.stream(pos)
					.mapToInt(reader::read)
					.filter(i -> i != -1)
					.mapToObj(i -> String.valueOf((char) i))
					.collect(Collectors.joining());
			assertEquals(expected, actual);
		}
	}

	static Stream<Arguments> readChars() {
		return Stream.of(//
				arguments("abc", "a", new long[]{0}),
				arguments("abc", "abc", new long[]{0, 1, 2}),
				arguments("abc", "abc", new long[]{0, 1, 2, 3, 4}),
				arguments("abc", "acb", new long[]{0, 2, 1}),
				arguments("abc", "acbb", new long[]{0, 2, 1, 1}),
				arguments("abc", "abb", new long[]{0, 1, 3, 1})
		);
	}

	@ParameterizedTest
	@MethodSource
	void readArrayOnce(final String s, final long start, final int length, final String expected) throws IOException {
		try (final var reader = new TestReader(s)) {
			final var chars = reader.read(start, length);
			assertEquals(expected, String.valueOf(chars));
		}
	}

	static Stream<Arguments> readArrayOnce() {
		return Stream.of(//
				arguments("abc", 0, 0, ""),
				arguments("abc", 0, 2, "ab"),
				arguments("abc", 0, 3, "abc"),
				arguments("abc", 0, 5, "abc"),
				arguments("abc", 2, 0, ""),
				arguments("abc", 2, 1, "c"),
				arguments("abc", 2, 5, "c")
		);
	}

	static class TestReader extends BufferedFileReader {

		final String s;

		TestReader(final String s) {
			super(null, null);
			this.s = s;
		}

		@Override
		Reader createReader() {
			return new StringReader(s);
		}

		@Override
		public long getLength() {
			return s.length();
		}
	}

}

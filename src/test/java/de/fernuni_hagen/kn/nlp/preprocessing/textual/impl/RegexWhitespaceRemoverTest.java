package de.fernuni_hagen.kn.nlp.preprocessing.textual.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class RegexWhitespaceRemoverTest {

	@ParameterizedTest
	@MethodSource
	void test(final CharSequence input, final String expected) {
		final var actual = new RegexWhitespaceRemover().removeWhitespace(input);
		assertEquals(expected, actual);
	}

	static Stream<Arguments> test() {
		return Stream.of(//
				arguments("abc abc", "abc abc"),
				arguments("abc\nabc", "abc abc"),
				arguments("abc  abc", "abc abc"),
				arguments("abc \nabc", "abc abc"),
				//arguments("\nabc\nabc", "abc abc"), // should only apply to the start of the text
				arguments("abc\n \nabc", "abc\nabc"),
				arguments("abc\n \n \nabc", "abc\nabc"),
				arguments("abc \n\nabc", "abc\nabc"),
				arguments("abc \n abc", "abc\nabc"),
				arguments("abc \n abc\n \n", "abc\nabc\n"),
				// CRLF
				arguments("abc\r\nabc", "abc abc")
		);
	}

}

package de.fernuni_hagen.kn.nlp.input.impl;

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
				arguments("abc\r\nabc\r\n", "abc abc"),
				arguments("abc\nabc \r\n", "abc abc")
		);
	}

}

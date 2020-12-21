package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class RegexCharacterRemoverTest {

	@ParameterizedTest
	@MethodSource
	void removeWhitespace(final CharSequence input, final String expected) {
		final var actual = new RegexCharacterRemover().removeCharacters(input);
		assertEquals(expected, actual);
	}

	static Stream<Arguments> removeWhitespace() {
		return Stream.of(//
				arguments("abc abc", "abc abc"),
				arguments("abc\nabc", "abc abc"),
				arguments("abc\r\nabc\r\n", "abc abc"),
				arguments("abc\nabc \r\n", "abc abc")
		);
	}

}

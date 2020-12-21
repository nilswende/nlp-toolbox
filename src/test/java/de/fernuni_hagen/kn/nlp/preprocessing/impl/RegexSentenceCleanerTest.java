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
class RegexSentenceCleanerTest {

	@ParameterizedTest
	@MethodSource
	void test(final CharSequence input, final String expected) {
		final var actual = new RegexSentenceCleaner().clean(input);
		assertEquals(expected, actual);
	}

	static Stream<Arguments> test() {
		return Stream.of(//
				arguments("abc abc", "abc abc"),
				arguments("abc11, abc", "abc11 abc"),
				arguments("abc?abc", "abcabc"),
				arguments("abcßabc", "abcßabc")
		);
	}

}

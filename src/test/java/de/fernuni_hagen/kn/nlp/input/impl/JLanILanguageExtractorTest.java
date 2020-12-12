package de.fernuni_hagen.kn.nlp.input.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.StringReader;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class JLanILanguageExtractorTest {

	@ParameterizedTest
	@MethodSource
	void test(final String sentence, final int inputLength, final Locale locale) {
		final int length = inputLength == -1 ? sentence.length() : inputLength;
		try (final var reader = new StringReader(sentence)) {
			final var language = new JLanILanguageExtractor().extract(reader, length);
			assertEquals(locale, language);
		}
	}

	static Stream<Arguments> test() {
		return Stream.of(
				arguments("my pony is over the ocean, my bonny is over the see", -1, Locale.ENGLISH),
				arguments("° und last but not least, bin ich ein _kurzer_ deutscher Satz (hubergel)!", -1, Locale.GERMAN),
				arguments("ein _kurzer_ deutscher Satz", 10, Locale.GERMAN),
				arguments("° und last but not least, bin ich ein _kurzer_ deutscher Satz (hubergel)!", 1000, Locale.GERMAN)
				// ,arguments("qwerty asdf yxcv", Locale.ENGLISH) // produces random language
		);
	}

}

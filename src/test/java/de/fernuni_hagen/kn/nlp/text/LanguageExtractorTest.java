package de.fernuni_hagen.kn.nlp.text;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class LanguageExtractorTest {

	@ParameterizedTest
	@MethodSource
	void test(final String sentence, final Locale locale) throws IOException {
		try (final var reader = new StringReader(sentence)) {
			final var language = new LanguageExtractor().extract(reader, sentence.length());
			assertEquals(locale, language);
		}
	}

	static Stream<Arguments> test() {
		return Stream.of(
				arguments("my pony is over the ocean, my bonny is over the see", Locale.ENGLISH),
				arguments("° und last but not least, bin ich ein _kurzer_ deutscher Satz (hubergel)!", Locale.GERMAN),
				arguments("qwerty asdf yxcv", Locale.ENGLISH)
		);
	}

}

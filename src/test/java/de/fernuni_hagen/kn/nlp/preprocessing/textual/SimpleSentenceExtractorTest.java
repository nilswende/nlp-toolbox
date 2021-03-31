package de.fernuni_hagen.kn.nlp.preprocessing.textual;

import de.fernuni_hagen.kn.nlp.TempFileTest;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.JLanILanguageExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.textual.impl.RegexWhitespaceRemover;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class SimpleSentenceExtractorTest extends TempFileTest {

	@ParameterizedTest
	@MethodSource
	void extract(final List<String> sentences) throws IOException {
		final String input = String.join(" ", sentences);
		writeString(input);
		final var languageExtractor = new JLanILanguageExtractor();
		final var extractor = new SimpleSentenceExtractor(languageExtractor.extract(tempFile), new RegexWhitespaceRemover());
		final var strings = extractor.extract(tempFile).collect(Collectors.toList());
		assertEquals(sentences, strings);
	}

	static Stream<Arguments> extract() {
		return Stream.of(
				arguments(List.of()),
				arguments(List.of("Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948.")),
				arguments(List.of("Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948.",
						"The competitions were part of the original intention of the Olympic Movement's founder, Pierre de Frédy, Baron de Coubertin.")),
				arguments(List.of("Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948.",
						"The competitions were part of the original intention of the Olympic Movement's founder, Pierre de Frédy, Baron de Coubertin.",
						"Medals were awarded for works of art inspired by sport, divided into five categories: architecture, literature, music, painting, and sculpture."))
		);
	}

}

package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import te.utils.Parameters;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class IndexerPhraseDetectorTest {

	@ParameterizedTest
	@MethodSource
	void extractPhrases(final List<String> sentences, final List<String> phrases) {
		final var extractedPhrases = new IndexerPhraseDetector(Parameters.EN).detectPhrases(sentences);
		assertTrue(phrases.containsAll(extractedPhrases), extractedPhrases.toString());
		assertTrue(extractedPhrases.containsAll(phrases), extractedPhrases.toString());
		assertTrue(extractedPhrases.stream().allMatch(
				p -> sentences.stream().anyMatch(s -> s.contains(p))
		), extractedPhrases.toString());
	}

	static Stream<Arguments> extractPhrases() {
		return Stream.of(
				arguments(List.of("Art competitions at the Olympic Games Art competitions at the Olympic Games Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948."),
						List.of("Art competitions", "Olympic Games")),
				arguments(List.of("Art competitions at the Olympic Games Art competitions at the Olympic Games Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948.",
						"The competitions were part of the original intention of the Olympic Movement's founder, Pierre de Frédy, Baron de Coubertin."),
						List.of("Art competitions", "Olympic Games"))
		);
	}

}

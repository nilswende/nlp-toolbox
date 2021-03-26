package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.PhraseExtractor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import te.utils.Parameters;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class IndexerPhraseExtractorTest {

	@ParameterizedTest
	@MethodSource
	void extractPhrases(final List<String> sentences, final List<String> phrases) {
		final var extractions = new IndexerPhraseExtractor(Parameters.EN)
				.extractPhrases(sentences.stream())
				.collect(Collectors.toList());
		extractions.forEach(ex -> assertTrue(ex.getPhrases().stream().noneMatch(p -> ex.getSentence().contains(p))));
		final var right = extractions.stream().map(PhraseExtractor.Extraction::getPhrases).flatMap(Collection::stream).collect(Collectors.toList());
		assertTrue(phrases.containsAll(right), right.toString());
		assertTrue(right.containsAll(phrases), right.toString());
	}

	static Stream<Arguments> extractPhrases() {
		return Stream.of(
				arguments(List.of("Art competitions at the Olympic Games Art competitions at the Olympic Games Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948."),
						List.of("Art competitions")),
				arguments(List.of("Art competitions at the Olympic Games Art competitions at the Olympic Games Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948.",
						"The competitions were part of the original intention of the Olympic Movement's founder, Pierre de Frédy, Baron de Coubertin."),
						List.of("Art competitions"))
		);
	}

}

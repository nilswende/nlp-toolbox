package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class PhrasedSentenceTest {

	@ParameterizedTest
	@MethodSource
	void getContent(final String sentence, final List<String> terms, final List<String> phrases) {
		getContentRemoved(sentence, terms, phrases, sentence);
	}

	static Stream<Arguments> getContent() {
		return Stream.of(
				// no phrases
				arguments("0 1 2 3 4",
						List.of("0", "1", "2", "3", "4"),
						List.of()),
				// single phrase
				arguments("ph 0 1 2 3 4",
						List.of("0", "1", "2", "3", "4"),
						List.of("ph")),
				arguments("0 1 ph 2 3 4",
						List.of("0", "1", "2", "3", "4"),
						List.of("ph")),
				arguments("0 1 2 3 4 ph",
						List.of("0", "1", "2", "3", "4"),
						List.of("ph")),
				// single phrase, duplicate terms
				arguments("0 1 1 ph 2 3 3 4",
						List.of("0", "1", "1", "2", "3", "3", "4"),
						List.of("ph")),
				// multiple phrases
				arguments("0 1 gg 2 3 4 ph",
						List.of("0", "1", "2", "3", "4"),
						List.of("ph", "gg")),
				arguments("0 1 ph gg 2 3 4 ph",
						List.of("0", "1", "2", "3", "4"),
						List.of("ph", "gg"))
		);
	}

	@ParameterizedTest
	@MethodSource
	void getContentRemoved(final String sentence, final List<String> terms, final List<String> phrases, final String expected) {
		final var tTerms = new ArrayList<TaggedTerm>();
		for (String term : terms) {
			tTerms.add(TaggedTerm.from(term + Tagset.STTS.getTagSeparator() + "test", Tagset.STTS));
		}

		final var phrasedSentence = new PhrasedSentence(tTerms, sentence, phrases);

		assertEquals(expected, phrasedSentence.getContent().collect(Collectors.joining(StringUtils.SPACE)));
	}

	static Stream<Arguments> getContentRemoved() {
		return Stream.of(
				// single phrase
				arguments("0 1 1 ph 2 3 3 4",
						List.of("0", "3", "3", "4"),
						List.of("ph"),
						"0 ph 3 3 4"),
				// multiple phrase
				arguments("gg 0 1 1 ph 2 3 3 4 gg",
						List.of("0", "3", "3", "4"),
						List.of("ph", "gg"),
						"gg 0 ph 3 3 4 gg"),
				// single term gets e.g. mistagged and removed
				arguments("gg 0 1 1 ph 2 3 3 4 gg",
						List.of("0", "1", "3", "3", "4"),
						List.of("ph", "gg"),
						"gg 0 1 ph 3 3 4 gg")
		);
	}
}

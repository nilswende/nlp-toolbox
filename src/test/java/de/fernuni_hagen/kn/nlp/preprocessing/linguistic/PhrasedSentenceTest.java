package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
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
				arguments("",
						List.of(),
						List.of()),
				// no phrases
				arguments("0 1 2 3 4",
						List.of("0", "1", "2", "3", "4"),
						List.of()),
				// single phrase
				arguments("aa 0 1 2 3 4",
						List.of("0", "1", "2", "3", "4"),
						List.of("aa")),
				arguments("0 1 aa 2 3 4",
						List.of("0", "1", "2", "3", "4"),
						List.of("aa")),
				arguments("0 1 2 3 4 aa",
						List.of("0", "1", "2", "3", "4"),
						List.of("aa")),
				// single phrase, duplicate terms
				arguments("0 1 1 aa 2 3 3 4",
						List.of("0", "1", "1", "2", "3", "3", "4"),
						List.of("aa")),
				// multiple phrases
				arguments("0 1 bb 2 3 4 aa",
						List.of("0", "1", "2", "3", "4"),
						List.of("aa", "bb")),
				arguments("0 1 aa bb 2 3 4 aa",
						List.of("0", "1", "2", "3", "4"),
						List.of("aa", "bb")),
				arguments("aa 0 1 bb 2 3 4 bb",
						List.of("0", "1", "2", "3", "4"),
						List.of("aa", "bb")),
				arguments("aa 0 bb 0",
						List.of("0", "0"),
						List.of("aa", "bb")),
				arguments("aa 0 1 1 bb 2 3 cc 4 5 1 6",
						List.of("0", "1", "1", "2", "3", "4", "5", "1", "6"),
						List.of("aa", "bb", "cc"))
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

		assertEquals(expected, String.join(StringUtils.SPACE, phrasedSentence.getContent()));
	}

	static Stream<Arguments> getContentRemoved() {
		return Stream.of(
				// single phrase
				arguments("0 1 1 aa 2 3 3 4",
						List.of("0", "3", "3", "4"),
						List.of("aa"),
						"0 aa 3 3 4"),
				// multiple phrase
				arguments("bb 0 1 1 aa 2 3 3 4 bb",
						List.of("0", "3", "3", "4"),
						List.of("aa", "bb"),
						"bb 0 aa 3 3 4 bb"),
				// single term gets e.g. mistagged and removed
				arguments("bb 0 1 1 aa 2 3 3 4 bb",
						List.of("0", "1", "3", "3", "4"),
						List.of("aa", "bb"),
						"bb 0 1 aa 3 3 4 bb")
		);
	}
}

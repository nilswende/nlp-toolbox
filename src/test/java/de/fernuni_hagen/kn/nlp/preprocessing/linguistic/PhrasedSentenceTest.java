package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

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
	void getContent(final String sentence, final List<String> phrases, final List<String> expected) {
		final var terms = new ArrayList<TaggedTerm>();
		for (int i = 0; i < 5; i++) {
			terms.add(TaggedTerm.from(i + Tagset.STTS.getTagSeparator() + "test", Tagset.STTS, i));
		}

		final var phrasedSentence = new PhrasedSentence(terms, sentence, phrases);

		assertEquals(expected, phrasedSentence.getContent().collect(Collectors.toList()));
	}

	static Stream<Arguments> getContent() {
		return Stream.of(
				arguments("0 1 2 3 4",
						List.of(),
						List.of("0", "1", "2", "3", "4")),
				arguments("0 1 ph 2 3 4",
						List.of("ph"),
						List.of("0", "1", "ph", "2", "3", "4"))
		);
	}
}

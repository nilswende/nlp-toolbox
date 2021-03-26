package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

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
class PhraseIteratorTest {

	@ParameterizedTest
	@MethodSource
	void next(final String sentence, final List<String> phrases, final List<String> expected) {
		final var iterator = new PhraseIterator(sentence, phrases);
		final var actual = new ArrayList<String>();
		int pos = -1;
		while (iterator.hasNext()) {
			final var next = iterator.next();
			actual.add(next);
			final var position = iterator.position();
			assertEquals(sentence.indexOf(next, pos + 1), position);
			pos = position;
		}
		assertEquals(expected, actual);
	}

	static Stream<Arguments> next() {
		return Stream.of(
				arguments("Art competitions at the Olympic Games Art competitions at the Olympic Games Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948.",
						List.of(),
						List.of()),
				arguments("Art competitions at the Olympic Games Art competitions at the Olympic Games Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948.",
						List.of("Art competitions"),
						List.of("Art competitions", "Art competitions", "Art competitions")),
				arguments("Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948. The competitions were part of the original intention of the Olympic Movement's founder, Pierre de Frédy, Baron de Coubertin. (Art competitions)",
						List.of("Art competitions", "Pierre de Frédy", "Baron de Coubertin"),
						List.of("Art competitions", "Pierre de Frédy", "Baron de Coubertin", "Art competitions"))
		);
	}

}

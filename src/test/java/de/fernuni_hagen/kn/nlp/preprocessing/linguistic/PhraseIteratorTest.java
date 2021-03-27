package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

	@ParameterizedTest
	@MethodSource
	void remove(final String sentence, final List<String> phrases, final String expectedSentence) {
		final var iterator = new PhraseIterator(sentence, phrases);
		iterator.removeAll();
		assertEquals(expectedSentence, iterator.getSentence(), sentence);
	}

	static Stream<Arguments> remove() {
		return Stream.of(
				arguments("Art competitions at the Olympic Games Art competitions at the Olympic Games Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948.",
						List.of(),
						"Art competitions at the Olympic Games Art competitions at the Olympic Games Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948."),
				arguments("Art competitions at the Olympic Games Art competitions at the Olympic Games Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948.",
						List.of("Art competitions"),
						"at the Olympic Games at the Olympic Games formed part of the modern Olympic Games during its early years, from 1912 to 1948."),
				// check whitespaces
				arguments("Art competitions, at the Olympic Games,Art competitions, formed part of the modern Olympic Games during its early years, from 1912 to 1948. The competitions were part of the original intention of the Olympic Movement's founder, Pierre de Frédy, Baron de Coubertin. Art competitions",
						List.of("Art competitions", "Pierre de Frédy", "Baron de Coubertin"),
						", at the Olympic Games,, formed part of the modern Olympic Games during its early years, from 1912 to 1948. The competitions were part of the original intention of the Olympic Movement's founder, , .")
		);
	}

	@Test
	void iterateViaNext() {
		final var iterator = new PhraseIterator("a a a", List.of("a"));
		int count = 0;
		while (true) { // never iterate like this
			try {
				iterator.next();
				count++;
			} catch (final RuntimeException e) {
				break;
			}
		}
		assertEquals(3, count);
	}

	@Test
	void nextThrows() {
		final var iterator = new PhraseIterator("", List.of());
		assertThrows(NoSuchElementException.class, iterator::next);
	}

	@Test
	void removeThrows() {
		final var iterator = new PhraseIterator("a", List.of("a"));
		assertThrows(IllegalStateException.class, iterator::remove);
		if (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
		assertThrows(IllegalStateException.class, iterator::remove);
	}

}

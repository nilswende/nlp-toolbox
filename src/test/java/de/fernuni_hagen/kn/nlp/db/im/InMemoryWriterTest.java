package de.fernuni_hagen.kn.nlp.db.im;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nils Wende
 */
class InMemoryWriterTest {

	InMemoryWriter writer = new InMemoryWriter();

	@Test
	void addSentence() {
		final var input = List.of("art", "competition", "game", "year");
		writer.addSentence(input);

		final var data = InMemoryDB.INSTANCE.getData();

		assertEquals(input.size(), data.size());
		data.forEach((k, v) -> {
			assertEquals(1, v.getCount());

			final var terms = new TreeSet<>(input);
			terms.remove(k);
			final var cooccs = v.getCooccs();
			assertEquals(terms, cooccs.keySet());
		});
	}

	@Test
	void add2Sentences() {
		final var input = List.of(
				List.of("art", "competition", "game", "year"),
				List.of("art", "artist", "amateur")
		);
		input.forEach(writer::addSentence);

		final var data = InMemoryDB.INSTANCE.getData();

		assertEquals(input.stream().mapToInt(List::size).sum() - 1, data.size());

		final var art = data.get("art");
		assertEquals(2, art.getCount());
		assertEquals(5, art.getCooccs().size());

		final var artist = data.get("artist");
		assertEquals(2, artist.getCooccs().size());
		final var game = data.get("game");
		assertEquals(3, game.getCooccs().size());

		data.entrySet().stream().filter(e -> !e.getKey().equals("art")).forEach(e -> assertEquals(1, e.getValue().getCount()));
	}

	@AfterEach
	void tearDown() {
		InMemoryDB.INSTANCE.deleteAll();
	}

}

package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.DBTest;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nils Wende
 */
public class DBTestIm extends DBTest {

	private static DBFactory dbFactory;

	private final InMemoryDB db = (InMemoryDB) dbFactory.getDb();

	@BeforeAll
	static void beforeAll() {
		dbFactory = createDbFactory(AppConfig.DbType.IN_MEMORY);
	}

	@Override
	protected DBFactory getDbFactory() {
		return dbFactory;
	}

	@Test
	void addSentence() {
		final var input = List.of("art", "competition", "game", "year");
		writer.addSentence(input);

		final var data = db.getData();

		assertEquals(input.size(), data.size());
		data.forEach((k, v) -> {
			assertEquals(1, v.getCount());
			assertEquals(1, v.getSentenceCount());

			final var terms = new HashSet<>(input);
			terms.remove(k);
			final var cooccs = v.getCooccs();
			assertEquals(terms, cooccs.keySet());
		});
	}

	@Test
	void add2Sentences() {
		final var input = List.of(
				List.of("art", "art", "competition", "game", "year"),
				List.of("art", "artist", "amateur")
		);
		input.forEach(writer::addSentence);

		final var data = db.getData();

		assertEquals(6, data.size());

		final var art = data.get("art");
		assertEquals(3, art.getCount());
		assertEquals(2, art.getSentenceCount());
		assertEquals(5, art.getCooccs().size()); // no coocc of itself

		final var artist = data.get("artist");
		assertEquals(2, artist.getCooccs().size());
		final var game = data.get("game");
		assertEquals(3, game.getCooccs().size());

		data.entrySet().stream().filter(e -> !e.getKey().equals("art")).forEach(e -> assertEquals(1, e.getValue().getCount()));
		data.entrySet().stream().filter(e -> !e.getKey().equals("art")).forEach(e -> assertEquals(1, e.getValue().getSentenceCount()));
	}

	@Test
	void persist() {
		final var input = List.of("art", "competition", "game", "year");
		writer.addSentence(input);

		dbFactory.close();
		dbFactory = createDbFactory(AppConfig.DbType.IN_MEMORY);

		assertTrue(getDbFactory().getReader().containsTerms(input));
	}
}

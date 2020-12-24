package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mockito;

import java.nio.file.Path;

/**
 * Initializes the DB before the first test class and resets it after each test.
 *
 * @author Nils Wende
 */
public abstract class DBTest {

	protected DBReader reader = DBFactory.instance().getReader();
	protected DBWriter writer = DBFactory.instance().getWriter();

	static {
		final var mock = Mockito.mock(Config.class);
		Mockito.when(mock.getDb()).thenReturn(Config.DB_IN_MEMORY);
		Mockito.when(mock.getInMemoryDbDir()).thenReturn(Path.of(""));
		DBFactory.init(mock);
	}

	@AfterEach
	void tearDown() {
		writer.deleteAll();
	}

}

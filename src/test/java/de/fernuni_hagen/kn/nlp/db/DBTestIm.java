package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import org.mockito.Mockito;

import java.nio.file.Path;

/**
 * @author Nils Wende
 */
public class DBTestIm extends DBTest {

	private static final DBFactory dbFactory;

	static {
		final var mock = Mockito.mock(AppConfig.class);
		Mockito.when(mock.getDb()).thenReturn(AppConfig.DB_IN_MEMORY);
		Mockito.when(mock.getInMemoryDbDir()).thenReturn(Path.of(""));
		dbFactory = DBFactory.from(mock);
	}

	@Override
	protected DBFactory getDbFactory() {
		return dbFactory;
	}
}

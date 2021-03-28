package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import org.mockito.Mockito;

/**
 * @author Nils Wende
 */
public abstract class DBTestNeo4J extends DBTest {

	protected static final DBFactory dbFactory;

	static {
		final var mock = Mockito.mock(AppConfig.class);
		Mockito.when(mock.getDb()).thenReturn(AppConfig.DB_NEO4J);
		dbFactory = DBFactory.from(mock);
	}

	@Override
	protected DBFactory getDbFactory() {
		return dbFactory;
	}
}

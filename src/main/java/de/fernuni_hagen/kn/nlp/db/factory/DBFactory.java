package de.fernuni_hagen.kn.nlp.db.factory;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.DB;

/**
 * Defines a factory for the database (Abstract Factory Pattern).<br>
 * This factory should be created only once to ensure the singleton property of the database it creates.<br>
 * That is why the database must only be accessed via instances created by this factory.
 *
 * @author Nils Wende
 */
public abstract class DBFactory implements AutoCloseable {

	DBFactory() {
	}

	/**
	 * Returns a DBReader instance for the current database.
	 *
	 * @return DBReader
	 */
	public abstract DBReader getReader();

	/**
	 * Returns a DBWriter instance for the current database.
	 *
	 * @return DBWriter
	 */
	public abstract DBWriter getWriter();

	/**
	 * Returns the concrete database implementation.<br>
	 * This should only be used for implementation-specific utility classes.<br>
	 * All general read/write operations should be performed by {@link DBReader}/{@link DBWriter}.
	 *
	 * @return the concrete database
	 */
	public abstract DB getDb();

	/**
	 * Creates a new factory for the database specified in the config.
	 *
	 * @param config AppConfig
	 * @return a new factory
	 */
	public static DBFactory from(final AppConfig config) {
		final var db = config.getDb();
		switch (db) {
			case IN_MEMORY:
				return new InMemoryDBFactory(config);
			case NEO4J:
				return new Neo4JDBFactory(config);
			default:
				throw new IllegalArgumentException("Unsupported DB: " + db);
		}
	}

	/**
	 * Closes the factory and shuts down the associated database.
	 */
	@Override
	public void close() {
		getDb().shutdown();
	}

}

package de.fernuni_hagen.kn.nlp.db.factory;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.AppConfig;

/**
 * Defines a factory for the database (Abstract Factory Pattern).<br>
 * This factory should be created only once to ensure the singleton property of the database it creates.<br>
 * That is why the database must only be accessed via instances created by this factory.
 *
 * @author Nils Wende
 */
public abstract class DBFactory {

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
	public abstract Object getDb();

	public static DBFactory from(final AppConfig config) {
		final var db = config.getDb();
		if (AppConfig.DB_IN_MEMORY.equalsIgnoreCase(db)) {
			return new InMemoryDBFactory(config);
		} else if (AppConfig.DB_NEO4J.equalsIgnoreCase(db)) {
			return new Neo4JDBFactory(config);
		}
		throw new IllegalArgumentException("Unsupported DB: " + db);
	}

}

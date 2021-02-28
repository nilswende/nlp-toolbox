package de.fernuni_hagen.kn.nlp.db.factory;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.AppConfig;

/**
 * Defines a factory for the database (Abstract Factory Pattern).<br>
 * This factory ensures the singleton property of the database it creates.<br>
 * That is why the database must only be accessed via instances created by this factory.
 *
 * @author Nils Wende
 */
public abstract class DBFactory {

	private static volatile DBFactory INSTANCE;

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

	/**
	 * Returns the singleton instance.
	 */
	// single-checked locking
	public static DBFactory instance() {
		var localRef = INSTANCE;
		if (localRef == null) {
			synchronized (DBFactory.class) {
				localRef = INSTANCE;
			}
		}
		return localRef;
	}

	/**
	 * Initializes the singleton instance with the given config.
	 *
	 * @param config AppConfig
	 */
	public static synchronized void init(final AppConfig config) {
		if (INSTANCE != null) {
			throw new AssertionError("init only once");
			// else instance() may fail to return the newer instance
			// the null check may only see the old instance
		}
		INSTANCE = initFactory(config);
	}

	private static DBFactory initFactory(final AppConfig config) {
		final var db = config.getDb();
		if (AppConfig.DB_IN_MEMORY.equalsIgnoreCase(db)) {
			return new InMemoryDBFactory(config);
		} else if (AppConfig.DB_NEO4J.equalsIgnoreCase(db)) {
			return new Neo4JDBFactory(config);
		}
		throw new IllegalArgumentException("Unsupported DB: " + db);
	}

}

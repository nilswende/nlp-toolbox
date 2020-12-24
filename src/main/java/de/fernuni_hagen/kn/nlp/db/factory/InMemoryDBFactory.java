package de.fernuni_hagen.kn.nlp.db.factory;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.db.im.InMemoryDB;
import de.fernuni_hagen.kn.nlp.db.im.InMemoryReader;
import de.fernuni_hagen.kn.nlp.db.im.InMemoryWriter;

/**
 * Concrete factory for the in-memory database.
 *
 * @author Nils Wende
 */
class InMemoryDBFactory extends DBFactory {

	private final InMemoryDB db;

	InMemoryDBFactory(final Config config) {
		this.db = new InMemoryDB(config);
	}

	@Override
	public DBReader getReader() {
		return new InMemoryReader(db);
	}

	@Override
	public DBWriter getWriter() {
		return new InMemoryWriter(db);
	}

	@Override
	public InMemoryDB getDb() {
		return db;
	}

}

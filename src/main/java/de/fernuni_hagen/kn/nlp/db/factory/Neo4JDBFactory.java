package de.fernuni_hagen.kn.nlp.db.factory;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.neo4j.Neo4J;
import de.fernuni_hagen.kn.nlp.db.neo4j.Neo4JReader;
import de.fernuni_hagen.kn.nlp.db.neo4j.Neo4JWriter;

/**
 * Concrete factory for the Neo4j graph database.
 *
 * @author Nils Wende
 */
class Neo4JDBFactory extends DBFactory {

	private final Neo4J db;

	Neo4JDBFactory(final AppConfig config) {
		super(config);
		this.db = new Neo4J(config);
	}

	@Override
	public DBReader getReader() {
		return new Neo4JReader(config, db);
	}

	@Override
	public DBWriter getWriter() {
		return new Neo4JWriter(db);
	}

	@Override
	public Neo4J getDb() {
		return db;
	}

}

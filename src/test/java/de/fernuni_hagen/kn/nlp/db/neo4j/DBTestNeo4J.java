package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.DBTest;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import de.fernuni_hagen.kn.nlp.utils.Utils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.neo4j.graphdb.Transaction;

/**
 * @author Nils Wende
 */
public class DBTestNeo4J extends DBTest {

	private static DBFactory dbFactory;

	@BeforeAll
	static void beforeAll() {
		dbFactory = createDbFactory(AppConfig.DbType.NEO4J);
	}

	@AfterAll
	static void afterAll() {
		dbFactory.close();
	}

	@Override
	protected DBFactory getDbFactory() {
		return dbFactory;
	}

	protected void printAllTerms() {
		try (final var tx = ((Neo4J) getDbFactory().getDb()).getGraphDb().beginTx()) {
			final var nodes = tx.getAllNodes();
			Utils.stream(nodes).map(EntityFormatter::formatNode).forEach(System.out::println);
			System.out.println();
		}
	}

	public void printAllRelationships() {
		try (final Transaction tx = ((Neo4J) getDbFactory().getDb()).getGraphDb().beginTx()) {
			tx.getAllRelationships().stream()
					.map(EntityFormatter::formatRelationship)
					.forEach(System.out::println);
			System.out.println();
		}
	}

}

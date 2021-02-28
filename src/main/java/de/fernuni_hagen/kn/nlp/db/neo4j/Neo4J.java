package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 * Neo4j graph database.<br>
 * <a href="https://neo4j.com/docs/java-reference/4.2/javadocs/">Neo4j Javadocs</a><br>
 * <a href="https://neo4j.com/docs/java-reference/current/java-embedded/">Using Neo4j embedded in Java applications</a><br>
 * <a href="https://neo4j.com/docs/cypher-manual/4.2/">Neo4j Cypher Manual v4.2</a><br>
 *
 * @author Nils Wende
 */
public class Neo4J {

	private static final String DEFAULT_DATABASE_NAME = "neo4j";
	private final GraphDatabaseService graphDb;

	public Neo4J(final AppConfig config) {
		final var managementService = new DatabaseManagementServiceBuilder(config.getNeo4JDbDir())
//				.setConfig(BoltConnector.enabled, true)
//				.setConfig(BoltConnector.listen_address, new SocketAddress("localhost", 7687))
				.build();
		graphDb = managementService.database(DEFAULT_DATABASE_NAME);
		stopDbOnShutdown(managementService);
		createUniqueConstraints();
	}

	private static void stopDbOnShutdown(final DatabaseManagementService managementService) {
		Runtime.getRuntime().addShutdownHook(new Thread(managementService::shutdown));
	}

	// also creates a single-property index on the constrained property
	private void createUniqueConstraints() {
		try (final Transaction tx = graphDb.beginTx()) {
			createUniqueNameConstraint(Labels.TERM, tx);
			createUniqueNameConstraint(Labels.SEQUENCE, tx);
			createUniqueIdConstraint(Labels.SENTENCE, tx);
			createUniqueIdConstraint(Labels.DOCUMENT, tx);
			tx.commit();
		}
	}

	private void createUniqueNameConstraint(final Labels label, final Transaction tx) {
		final var stmt = "CREATE CONSTRAINT unique" + label + "Names IF NOT EXISTS\n" +
				"ON (l:" + label + ") ASSERT l.name IS UNIQUE\n";
		StatementPrinter.print(stmt);
		tx.execute(stmt);
	}

	private void createUniqueIdConstraint(final Labels label, final Transaction tx) {
		final var stmt = "CREATE CONSTRAINT unique" + label + "Ids IF NOT EXISTS\n" +
				"ON (l:" + label + ") ASSERT l.id IS UNIQUE\n";
		StatementPrinter.print(stmt);
		tx.execute(stmt);
	}

	private void dropConstraint(final String name) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var stmt = "DROP CONSTRAINT " + name;
			tx.execute(stmt);
			tx.commit();
		}
	}

	public GraphDatabaseService getGraphDb() {
		return graphDb;
	}

}

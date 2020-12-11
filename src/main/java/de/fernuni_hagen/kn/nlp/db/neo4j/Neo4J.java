package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.DB;
import de.fernuni_hagen.kn.nlp.config.Config;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Neo4j graph database.<br>
 * <a href="https://neo4j.com/docs/java-reference/current/java-embedded/">Neo4J docs</a>
 *
 * @author Nils Wende
 */
// if it's used, should be a singleton
public class Neo4J implements DB {

	private static final String DEFAULT_DATABASE_NAME = "neo4j";
	private static Neo4J INSTANCE;
	private final GraphDatabaseService graphDb;

	public Neo4J(final Config config) {
		final var managementService = new DatabaseManagementServiceBuilder(config.getDbDir()).build();
		graphDb = managementService.database(DEFAULT_DATABASE_NAME);
		stopDbOnShutdown(managementService);
		createUniqueConstraints();
	}

	private static void stopDbOnShutdown(final DatabaseManagementService managementService) {
		Runtime.getRuntime().addShutdownHook(new Thread(managementService::shutdown));
	}

	private void createUniqueConstraints() {
		try (final Transaction tx = graphDb.beginTx()) {
			var stmt = "DROP CONSTRAINT words\n";
			tx.execute(stmt);
			stmt = "CREATE CONSTRAINT uniqueTermNames IF NOT EXISTS\n" +
					"ON (term:" + Labels.TERM + ") ASSERT term.name IS UNIQUE\n";
			tx.execute(stmt);
			tx.commit();
		}
	}

	public void deleteAll() {
		try (final Transaction tx = graphDb.beginTx()) {
			final var stmt = "MATCH (n)\n" +
					"DETACH DELETE n\n";
			tx.execute(stmt);
			tx.commit();
		}
	}

	@Override
	public void addSentence(final List<String> terms) {
		try (final Transaction tx = graphDb.beginTx()) {
			addNodes(terms, tx);
			addRelationships(terms, tx);
			tx.commit();
		}
	}

	private void addNodes(final List<String> terms, final Transaction tx) {
		final var stmt = "MERGE (term:" + Labels.TERM + " {name: $name})\n" +
				"ON CREATE SET term.name = $name, term.count = 1\n" +
				"ON MATCH SET term.count = term.count + 1\n";
		for (final String term : terms) {
			tx.execute(stmt, Map.of("name", term));
		}
	}

	private void addRelationships(final List<String> terms, final Transaction tx) {
		final var stmt = "MATCH (term1:" + Labels.TERM + " {name: $name1}),(term2:" + Labels.TERM + " {name: $name2})\n" +
				"MERGE (term1)-[r:" + RelationshipTypes.CONNECTED + "]-(term2)\n" +
				"ON CREATE SET r.count = 1, r.dice = 0, r.cost = 0\n" +
				"ON MATCH SET r.count = r.count + 1\n";
		for (int i = 0; i < terms.size(); i++) {
			final var term1 = terms.get(i);
			for (int j = i + 1; j < terms.size(); j++) {
				final var term2 = terms.get(j);
				if (!term1.equals(term2)) {
					tx.execute(stmt, Map.of("name1", term1, "name2", term2));
				}
			}
		}
	}

	@Override
	public void updateDiceAndCosts() {
		final var stmt = "MATCH (term1:" + Labels.TERM + ")-[r:" + RelationshipTypes.CONNECTED + "]-(term2:" + Labels.TERM + ")\n" +
				"RETURN term1.count, term2.count, r.count, r";
		try (final Transaction tx = graphDb.beginTx();
			 final var result = tx.execute(stmt)) {
			while (result.hasNext()) {
				final var row = result.next();
				final var dice = calculateDice(row);
				final var relationship = (Relationship) row.get("r");
				relationship.setProperty("dice", dice);
				relationship.setProperty("cost", 1 / (dice + 0.01));
			}
			tx.commit();
		}
	}

	private double calculateDice(final Map<String, Object> row) {
		final var a = toLong(row.get("term1.count"));
		final var b = toLong(row.get("term2.count"));
		var ab = toLong(row.get("r.count"));
		ab = Math.min(ab, Math.min(a, b));
		return Math.min(1, (2 * ab) / (double) (a + b));
	}

	private long toLong(final Object o) {
		return ((Number) o).longValue();
	}

	public List<String> getAllNodes() {
		try (final Transaction tx = graphDb.beginTx()) {
			final var nodes = tx.getAllNodes();
			return nodes.stream()
					.map(n -> n.getAllProperties().toString())
					.collect(Collectors.toList());
		}
	}

	public List<String> getAllRelationships() {
		try (final Transaction tx = graphDb.beginTx()) {
			final var relationships = tx.getAllRelationships();
			return relationships.stream()
					.map(r -> r.getStartNode() + " " + r.getAllProperties().toString() + " " + r.getEndNode())
					.collect(Collectors.toList());
		}
	}

	public static Neo4J instance() {
		return INSTANCE;
	}

	public static Neo4J init(final Config config) {
		if (INSTANCE != null) {
			throw new AssertionError();
		}
		return INSTANCE = new Neo4J(config);
	}

}

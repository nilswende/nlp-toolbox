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
			final var schema = tx.schema();
			final var name = "words";
			try { // check if constraint exists
				schema.getConstraintByName(name);
			} catch (final IllegalArgumentException e) {
				schema // if not, create it
						.constraintFor(Labels.WORD)
						.assertPropertyIsUnique("name")
						.withName(name)
						.create();
			}
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
	public void addSentence(final List<String> words) {
		try (final Transaction tx = graphDb.beginTx()) {
			addNodes(words, tx);
			addRelationships(words, tx);
			tx.commit();
		}
	}

	private void addNodes(final List<String> words, final Transaction tx) {
		final var stmt = "MERGE (word:" + Labels.WORD + " {name: $name})\n" +
				"ON CREATE SET word.name = $name, word.count = 1\n" +
				"ON MATCH SET word.count = word.count + 1\n";
		for (final String word : words) {
			tx.execute(stmt, Map.of("name", word));
		}
	}

	private void addRelationships(final List<String> words, final Transaction tx) {
		final var stmt = "MATCH (word1:" + Labels.WORD + " {name: $name1}),(word2:" + Labels.WORD + " {name: $name2})\n" +
				"MERGE (word1)-[r:" + RelationshipTypes.CONNECTED + "]-(word2)\n" +
				"ON CREATE SET r.count = 1, r.dice = 0, r.cost = 0\n" +
				"ON MATCH SET r.count = r.count + 1\n";
		for (int i = 0; i < words.size(); i++) {
			final var word1 = words.get(i);
			for (int j = i + 1; j < words.size(); j++) {
				final var word2 = words.get(j);
				if (!word1.equals(word2)) {
					tx.execute(stmt, Map.of("name1", word1, "name2", word2));
				}
			}
		}
	}

	@Override
	public void updateDiceAndCosts() {
		final var stmt = "MATCH (word1:" + Labels.WORD + ")-[r:" + RelationshipTypes.CONNECTED + "]-(word2:" + Labels.WORD + ")\n" +
				"RETURN word1.count, word2.count, r";
		try (final Transaction tx = graphDb.beginTx();
			 final var result = tx.execute(stmt)) {
			while (result.hasNext()) {
				final var row = result.next();
				final var r = (Relationship) row.get("r");
				final var o = row.get("word1.count");
				final var a = toLong(o);
				final var b = toLong(row.get("word2.count"));
				var ab = toLong(r.getProperty("count"));
				ab = Math.min(ab, Math.min(a, b));
				final var dice = Math.min(1, (2 * ab) / (double) (a + b));
				r.setProperty("dice", dice);
				r.setProperty("cost", 1 / (dice + 0.01));
			}
			tx.commit();
		}
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

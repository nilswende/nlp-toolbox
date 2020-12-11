package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.DB;
import de.fernuni_hagen.kn.nlp.config.Config;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private Node currentDocument;

	public Neo4J(final Config config) {
		final var managementService = new DatabaseManagementServiceBuilder(config.getDbDir()).build();
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
			final var stmt = "CREATE CONSTRAINT uniqueTermNames IF NOT EXISTS\n" +
					"ON (t:" + Labels.TERM + ") ASSERT t.name IS UNIQUE\n";
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
	public void addDocument(final File file) {
		try (final Transaction tx = graphDb.beginTx()) {
			currentDocument = tx.createNode(Labels.DOCUMENT);
			currentDocument.setProperty("name", file.getName());
			tx.commit();
		}
	}

	@Override
	public void addSentence(final List<String> terms) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var termNodes = addTermNodes(terms, tx);
			addTermRelationships(terms, tx);
			addSentenceNode(termNodes, tx);
			tx.commit();
		}
	}

	private List<Node> addTermNodes(final List<String> terms, final Transaction tx) {
		final var stmt = "MERGE (t:" + Labels.TERM + " {name: $name})\n" +
				"ON CREATE SET t.name = $name, t.count = 1\n" +
				"ON  MATCH SET t.count = t.count + 1\n" +
				"RETURN t\n";
		return terms.stream()
				.flatMap(term -> addTerm(term, stmt, tx))
				.collect(Collectors.toList());
	}

	private Stream<Node> addTerm(final String term, final String stmt, final Transaction tx) {
		try (final var result = tx.execute(stmt, Map.of("name", term))) {
			return result.stream()
					.map(Map::values)
					.flatMap(Collection::stream)
					.map(v -> (Node) v);
		}
	}

	private void addTermRelationships(final List<String> terms, final Transaction tx) {
		final var stmt = "MATCH (t1:" + Labels.TERM + " {name: $name1}),(t2:" + Labels.TERM + " {name: $name2})\n" +
				"MERGE (t1)-[r:" + RelationshipTypes.COOCCURS + "]-(t2)\n" +
				"ON CREATE SET r.count = 1\n" +
				"ON  MATCH SET r.count = r.count + 1\n";
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

	private void addSentenceNode(final List<Node> termNodes, final Transaction tx) {
		final var s = tx.createNode(Labels.SENTENCE);
		termNodes.forEach(term -> s.createRelationshipTo(term, RelationshipTypes.CONTAINS));
		currentDocument.createRelationshipTo(s, RelationshipTypes.CONTAINS);
	}

	@Override
	public void updateDiceAndCosts() {
		final var stmt = "MATCH (t1:" + Labels.TERM + ")-[r:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n" +
				"RETURN t1.count, t2.count, r.count, r";
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
		final var a = toLong(row.get("t1.count"));
		final var b = toLong(row.get("t2.count"));
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

package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.math.WeightingFunctions;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static de.fernuni_hagen.kn.nlp.db.neo4j.Utils.toLong;

/**
 * Neo4j graph database.<br>
 * <a href="https://neo4j.com/docs/java-reference/4.2/javadocs/">Neo4J Javadocs</a><br>
 * <a href="https://neo4j.com/docs/java-reference/current/java-embedded/">Using Neo4j embedded in Java applications</a><br>
 * <a href="https://neo4j.com/docs/cypher-manual/4.2/">The Neo4j Cypher Manual v4.2</a><br>
 *
 * @author Nils Wende
 */
// if it's used, should be a singleton
public class Neo4J implements DBWriter, DBReader {

	private static final String DEFAULT_DATABASE_NAME = "neo4j";
	private static Neo4J INSTANCE;
	private final GraphDatabaseService graphDb;
	private final Sequences sequences;
	private long currentDocId;

	public Neo4J(final Config config) {
		final var managementService = new DatabaseManagementServiceBuilder(config.getDbDir()).build();
		graphDb = managementService.database(DEFAULT_DATABASE_NAME);
		sequences = new Sequences(graphDb);
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
			tx.commit();
		}
	}

	private void createUniqueNameConstraint(final Labels label, final Transaction tx) {
		final var stmt = "CREATE CONSTRAINT unique" + label + "Names IF NOT EXISTS\n" +
				"ON (l:" + label + ") ASSERT l.name IS UNIQUE\n";
		tx.execute(stmt);
	}

	private void dropConstraint(final String name) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var stmt = "DROP CONSTRAINT " + name;
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
		currentDocId = sequences.nextValueFor(Labels.DOCUMENT);
		try (final Transaction tx = graphDb.beginTx()) {
			final Node doc = tx.createNode(Labels.DOCUMENT);
			doc.setProperty("name", file.getName());
			doc.setProperty("id", currentDocId);
			tx.commit();
		}
	}

	@Override
	public void addSentence(final List<String> terms) {
		new SentenceAdder(graphDb).addSentence(terms, currentDocId);
	}

	@Override
	public Map<String, Map<String, Double>> getSignificances(final WeightingFunction function) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var k = countSentences(tx);
			final var matchCooccs = "MATCH (:" + Labels.SENTENCE + ")-[s1:" + RelationshipTypes.CONTAINS + "]-(t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")-[s2:" + RelationshipTypes.CONTAINS + "]-(:" + Labels.SENTENCE + ")\n" +
					"RETURN t1.name, t2.name, s1.count as ki, s2.count as kj, c.count as kij";
			try (final var result = tx.execute(matchCooccs)) {
				final var map = new TreeMap<String, Map<String, Double>>();
				while (result.hasNext()) {
					final var row = result.next();
					getSig(row, k, function, map);
				}
				return map;
			}
		}
	}

	private void getSig(final Map<String, Object> row, final long k, final WeightingFunction function, final Map<String, Map<String, Double>> map) {
		final var t1 = row.get("t1.name").toString();
		final var t2 = row.get("t2.name").toString();
		final double sig = calcSig(row, k, function);
		map.computeIfAbsent(t1, t -> new TreeMap<>()).put(t2, sig);
	}

	private double calcSig(final Map<String, Object> row, final long k, final WeightingFunction function) {
		final var ki = toLong(row.get("ki"));
		final var kj = toLong(row.get("kj"));
		final var kij = toLong(row.get("kij"));
		return function.calculate(ki, kj, kij, k);
	}

	private long countSentences(final Transaction tx) {
		final var countSentences = "MATCH (s:" + Labels.SENTENCE + ")\n" +
				"RETURN count(*)";
		try (final var result = tx.execute(countSentences)) {
			final var row = result.next();
			return toLong(row.get("count(*)"));
		}
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
		return Math.min(1, WeightingFunctions.DICE.calculate(a, b, ab, 0));
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

package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static de.fernuni_hagen.kn.nlp.db.neo4j.Utils.toLong;

/**
 * Implements reading from the Neo4j graph database.
 *
 * @author Nils Wende
 */
public class Neo4JReader implements DBReader {

	private final GraphDatabaseService graphDb = Neo4J.instance().getGraphDb();

	@Override
	public Map<String, List<String>> getCooccurrences() {
		try (final Transaction tx = graphDb.beginTx()) {
			final var matchCooccs = "MATCH (t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n" +
					"RETURN t1.name, t2.name";
			try (final var result = tx.execute(matchCooccs)) {
				final var map = new TreeMap<String, List<String>>();
				while (result.hasNext()) {
					final var row = result.next();
					final var t1 = row.get("t1.name").toString();
					final var t2 = row.get("t2.name").toString();
					map.computeIfAbsent(t1, t -> new ArrayList<>()).add(t2);
				}
				return map;
			}
		}
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

}

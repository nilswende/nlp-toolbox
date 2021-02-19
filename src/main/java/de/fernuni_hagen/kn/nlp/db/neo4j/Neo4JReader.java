package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.math.DirectedWeightingFunction;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static de.fernuni_hagen.kn.nlp.db.neo4j.Neo4JUtils.toDouble;
import static de.fernuni_hagen.kn.nlp.db.neo4j.Neo4JUtils.toLong;

/**
 * Implements reading from the Neo4j graph database.
 *
 * @author Nils Wende
 */
public class Neo4JReader implements DBReader {

	private final GraphDatabaseService graphDb;

	public Neo4JReader(final Neo4J db) {
		graphDb = db.getGraphDb();
	}

	@Override
	public Map<String, Map<String, Double>> getCooccurrences() {
		try (final Transaction tx = graphDb.beginTx()) {
			final var matchCooccs = "MATCH (t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n" +
					"RETURN t1.name, t2.name, c.count\n";
			try (final var result = tx.execute(matchCooccs)) {
				final var map = new TreeMap<String, Map<String, Double>>();
				while (result.hasNext()) {
					final var row = result.next();
					final var t1 = row.get("t1.name").toString();
					final var t2 = row.get("t2.name").toString();
					final var c = toDouble(row.get("c.count"));
					map.computeIfAbsent(t1, t -> new TreeMap<>()).put(t2, c);
				}
				return map;
			}
		}
	}

	@Override
	public Map<String, Map<String, Double>> getSignificances(final WeightingFunction function) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var k = countSentences(tx);
			final var matchCooccs = " MATCH (t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n" +
					"RETURN t1.name, t2.name, t1.count as ki, t2.count as kj, c.count as kij\n";
			try (final var result = tx.execute(matchCooccs)) {
				final var map = new TreeMap<String, Map<String, Double>>();
				while (result.hasNext()) {
					final var row = result.next();
					final double sig = calcSig(row, k, function);
					putSig(row, sig, map);
				}
				return map;
			}
		}
	}

	private void putSig(final Map<String, Object> row, final double sig, final Map<String, Map<String, Double>> map) {
		final var t1 = row.get("t1.name").toString();
		final var t2 = row.get("t2.name").toString();
		map.computeIfAbsent(t1, t -> new TreeMap<>()).put(t2, sig);
	}

	private double calcSig(final Map<String, Object> row, final long k, final WeightingFunction function) {
		final var ki = toLong(row.get("ki"));
		final var kj = toLong(row.get("kj"));
		final var kij = toLong(row.get("kij"));
		return function.calculate(ki, kj, kij, k);
	}

	private long countSentences(final Transaction tx) {
		final var countSentences = " MATCH (s:" + Labels.SENTENCE + ")\n" +
				"RETURN count(*)\n";
		try (final var result = tx.execute(countSentences)) {
			final var row = result.next();
			return toLong(row.get("count(*)"));
		}
	}

	@Override
	public Map<String, Map<String, Double>> getSignificances(final DirectedWeightingFunction function) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var kmax = getMaxSentencesCount(tx);
			final var matchCooccs = " MATCH (t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n" +
					" WHERE (c.count / t1.count) >= (c.count / t2.count)\n" +
					"RETURN t1.name, t2.name, c.count as kij\n";
			try (final var result = tx.execute(matchCooccs)) {
				final var map = new TreeMap<String, Map<String, Double>>();
				while (result.hasNext()) {
					final var row = result.next();
					final var t1 = row.get("t1.name").toString();
					final var t2 = row.get("t2.name").toString();
					if (!map.containsKey(t2) || !map.get(t2).containsKey(t1)) {
						final double sig = calcSig(row, kmax, function);
						map.computeIfAbsent(t1, t -> new TreeMap<>()).put(t2, sig);
					}
				}
				return map;
			}
		}
	}

	private double calcSig(final Map<String, Object> row, final long kmax, final DirectedWeightingFunction function) {
		final var kij = toLong(row.get("kij"));
		return function.calculate(kij, kmax);
	}

	private long getMaxSentencesCount(final Transaction tx) {
		final var countSentences = " MATCH (:" + Labels.SENTENCE + ")-[s:" + RelationshipTypes.CONTAINS + "]-(:" + Labels.TERM + ")\n" +
				"RETURN max(s.count) as max\n";
		try (final var result = tx.execute(countSentences)) {
			final var row = result.next();
			return toLong(row.get("max"));
		}
	}

	public List<String> getAllNodes() {
		try (final Transaction tx = graphDb.beginTx()) {
			return tx.getAllNodes().stream()
					.map(EntityFormatter::formatNode)
					.collect(Collectors.toList());
		}
	}

	public List<String> getAllRelationships() {
		try (final Transaction tx = graphDb.beginTx()) {
			return tx.getAllRelationships().stream()
					.map(EntityFormatter::formatRelationship)
					.collect(Collectors.toList());
		}
	}

	public void printPath(final String t1, final String t2) {
		final var stmt = " MATCH (t1:" + Labels.TERM + " {name: $t1}),\n" +
				"       (t2:" + Labels.TERM + " {name: $t2}),\n" +
				"       p = shortestPath((t1)-[:" + RelationshipTypes.COOCCURS + "*]-(t2))\n" +
				"RETURN p\n";
		try (final Transaction tx = graphDb.beginTx()) {
			final Map<String, Object> params = Map.of("t1", t1, "t2", t2);
			tx.execute(stmt, params).stream()
					.map(map -> ((Path) map.get("p")))
					.map(EntityFormatter::formatPath)
					.forEach(System.out::println);
		}
	}

	@Override
	public MultiKeyMap<String, Double> getTermFrequencies() {
		final var map = new MultiKeyMap<String, Double>();
		return map;//TODO
	}
}

package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.math.DirectedWeightingFunctions;
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
					"RETURN t1.name, t2.name\n";
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
			final var matchCooccs = " MATCH (:" + Labels.SENTENCE + ")-[s1:" + RelationshipTypes.CONTAINS + "]-(t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")-[s2:" + RelationshipTypes.CONTAINS + "]-(:" + Labels.SENTENCE + ")\n" +
					"RETURN t1.name, t2.name, s1.count as ki, s2.count as kj, c.count as kij\n";
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
	public Map<String, Map<String, Double>> getSignificances(final DirectedWeightingFunctions function) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var kmax = getMaxSentencesCount(tx);
			final var matchCooccs = " MATCH (:" + Labels.SENTENCE + ")-[s1:" + RelationshipTypes.CONTAINS + "]-(t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")-[s2:" + RelationshipTypes.CONTAINS + "]-(:" + Labels.SENTENCE + ")\n" +
					"  WITH t1.name as t1name, t2.name as t2name, c.count as kij, sum(s1.count) as scount1, sum(s2.count) as scount2\n" +
					" WHERE (kij / scount1) >= (kij / scount2)\n" + //TODO case = appears twice
					"RETURN t1name, t2name, kij\n";
			try (final var result = tx.execute(matchCooccs)) {
				final var map = new TreeMap<String, Map<String, Double>>();
				while (result.hasNext()) {
					final var row = result.next();
					final double sig = calcSig(row, kmax, function);
					putDominantSig(row, sig, map);
				}
				return map;
			}
		}
	}

	private double calcSig(final Map<String, Object> row, final long kmax, final DirectedWeightingFunctions function) {
		final var kij = toLong(row.get("kij"));
		return function.calculate(kij, kmax);
	}

	private void putDominantSig(final Map<String, Object> row, final double sig, final Map<String, Map<String, Double>> map) {
		final var t1 = row.get("t1name").toString();
		final var t2 = row.get("t2name").toString();
		if (map.containsKey(t2)) {
			if (map.get(t2).containsKey(t1)) {
				return;  // workaround case = appears twice
			}
		}
		map.computeIfAbsent(t1, t -> new TreeMap<>()).put(t2, sig);
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

}

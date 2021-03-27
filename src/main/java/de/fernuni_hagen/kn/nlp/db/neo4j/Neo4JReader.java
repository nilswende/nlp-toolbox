package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.db.DBUtils;
import de.fernuni_hagen.kn.nlp.graph.WeightedPath;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
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
			final var matchCooccs = "MATCH (t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n"
					+ "RETURN t1.name, t2.name, c.count\n";
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
			final var kmax = getMaxSentencesCount(tx);
			final var matchCooccs = " MATCH (t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n"
					+ "RETURN t1.name, t2.name, t1.count as ki, t2.count as kj, c.count as kij\n";
			try (final var result = tx.execute(matchCooccs)) {
				final var map = new TreeMap<String, Map<String, Double>>();
				while (result.hasNext()) {
					final var row = result.next();
					final double sig = calcSig(row, k, kmax, function);
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

	private double calcSig(final Map<String, Object> row, final long k, final long kmax, final WeightingFunction function) {
		final var ki = toLong(row.get("ki"));
		final var kj = toLong(row.get("kj"));
		final var kij = toLong(row.get("kij"));
		return function.calculate(ki, kj, kij, k, kmax);
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
	public Map<String, Map<String, Double>> getDirectedSignificances(final WeightingFunction function) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var k = countSentences(tx);
			final var kmax = getMaxSentencesCount(tx);
			final var matchCooccs = " MATCH (t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n"
					+ " WHERE (c.count / t1.count) >= (c.count / t2.count)\n"
					+ "RETURN t1.name, t2.name, t1.count as ki, t2.count as kj, c.count as kij\n";
			try (final var result = tx.execute(matchCooccs)) {
				final var map = new TreeMap<String, Map<String, Double>>();
				while (result.hasNext()) {
					final var row = result.next();
					final var t1 = row.get("t1.name").toString();
					final var t2 = row.get("t2.name").toString();
					if (!map.containsKey(t2) || !map.get(t2).containsKey(t1)) {
						final double sig = calcSig(row, k, kmax, function);
						map.computeIfAbsent(t1, t -> new TreeMap<>()).put(t2, sig);
					}
				}
				return map;
			}
		}
	}

	/**
	 * Returns the maximum number of sentences that contain the same term.
	 */
	private long getMaxSentencesCount(final Transaction tx) {
		final var stmt = " MATCH (:" + Labels.SENTENCE + ")-[s:" + RelationshipTypes.CONTAINS + "]-(:" + Labels.TERM + ")\n"
				+ "RETURN max(s.count) as max\n";
		try (final var result = tx.execute(stmt)) {
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
		final var stmt = " MATCH (t1:" + Labels.TERM + " {name: $t1}),\n"
				+ "       (t2:" + Labels.TERM + " {name: $t2}),\n"
				+ "       p = shortestPath((t1)-[:" + RelationshipTypes.COOCCURS + "*]-(t2))\n"
				+ "RETURN p\n";
		try (final Transaction tx = graphDb.beginTx()) {
			final Map<String, Object> params = Map.of("t1", t1, "t2", t2);
			tx.execute(stmt, params).stream()
					.map(map -> ((Path) map.get("p")))
					.map(EntityFormatter::formatPath)
					.forEach(System.out::println);
		}
	}

	@Override
	public Map<String, Map<String, Long>> getTermFrequencies() {
		return null;//TODO
	}

	@Override
	public WeightedPath getShortestPath(final String start, final String end, final WeightingFunction function) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var k = countSentences(tx);
			final var kmax = getMaxSentencesCount(tx);
			final var evaluator = new SignificanceEvaluator(k, kmax, function);
			final var pathFinder = GraphAlgoFactory.dijkstra(PathExpanders.forType(RelationshipTypes.COOCCURS), evaluator, 1);
			final var path = pathFinder.findSinglePath(tx.findNode(Labels.TERM, "name", start), tx.findNode(Labels.TERM, "name", end));
			final var nodes = Neo4JUtils.stream(path.nodes()).map(n -> n.getProperty("name").toString()).collect(Collectors.toList());
			return new WeightedPath(nodes, path.weight());
		}
	}

	@Override
	public List<List<String>> getAllSentencesInDocument(final java.nio.file.Path path) {
		final var stmt = "   MATCH (:" + Labels.DOCUMENT + " {name: $doc})-[r:" + RelationshipTypes.CONTAINS + "]-(:" + Labels.SENTENCE + ")-[p:" + RelationshipTypes.CONTAINS + "]-(t:" + Labels.TERM + ")\n"
				+ "  RETURN r.position, t\n"
				+ "ORDER BY r.position, p.position\n";
		final Map<String, Object> params = Map.of("doc", DBUtils.normalizePath(path));
		try (final Transaction tx = graphDb.beginTx();
			 final var result = tx.execute(stmt, params)) {
			return new ArrayList<>(result.stream()
					.collect(Collectors.groupingBy(row -> row.get("r.position"), Collectors.mapping(row -> row.get("t").toString(), Collectors.toList())))
					.values());
		}
	}

	private static class SignificanceEvaluator implements CostEvaluator<Double> {
		private final long k;
		private final long kmax;
		private final WeightingFunction function;

		SignificanceEvaluator(final long k, final long kmax, final WeightingFunction function) {
			this.k = k;
			this.kmax = kmax;
			this.function = function;
		}

		@Override
		public Double getCost(final Relationship relationship, final Direction direction) {
			final var ki = toLong(relationship.getStartNode().getProperty("count"));
			final var kj = toLong(relationship.getEndNode().getProperty("count"));
			final var kij = toLong(relationship.getProperty("count"));
			return 1 / function.calculate(ki, kj, kij, k, kmax);
		}
	}

}

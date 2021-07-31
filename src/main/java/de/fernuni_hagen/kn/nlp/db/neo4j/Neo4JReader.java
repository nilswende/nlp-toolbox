package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.DBUtils;
import de.fernuni_hagen.kn.nlp.graph.WeightedPath;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Utils;
import org.apache.commons.lang3.ObjectUtils;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
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

	private final AppConfig config;
	private final GraphDatabaseService graphDb;

	public Neo4JReader(final AppConfig config, final Neo4J db) {
		this.config = config;
		graphDb = db.getGraphDb();
	}

	@Override
	public Map<String, Map<String, Double>> getCooccurrences() {
		final var stmt = " MATCH (t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n"
				+ "RETURN t1.name, t2.name, c.count\n";
		try (final Transaction tx = graphDb.beginTx();
			 final var result = tx.execute(stmt)) {
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

	@Override
	public Map<String, Double> getCooccurrences(final String term) {
		final var stmt = " MATCH (t1:" + Labels.TERM + "{name: $t1})-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n"
				+ "RETURN t2.name, c.count\n";
		final Map<String, Object> params = Map.of("t1", term);
		try (final Transaction tx = graphDb.beginTx();
			 final var result = tx.execute(stmt, params)) {
			final var map = new TreeMap<String, Double>();
			while (result.hasNext()) {
				final var row = result.next();
				final var t2 = row.get("t2.name").toString();
				final var c = toDouble(row.get("c.count"));
				map.put(t2, c);
			}
			return map;
		}
	}

	@Override
	public Map<String, Map<String, Double>> getSignificances(final WeightingFunction function) {
		final var stmt = " MATCH (t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n"
				+ "RETURN t1.name, t2.name, t1.count as ki, t2.count as kj, c.count as kij\n";
		try (final Transaction tx = graphDb.beginTx();
			 final var result = tx.execute(stmt)) {
			final var k = countSentences(tx);
			final var kmax = getMaxCount(tx);
			final var map = new TreeMap<String, Map<String, Double>>();
			while (result.hasNext()) {
				final var row = result.next();
				final var ki = toLong(row.get("ki"));
				final var kj = toLong(row.get("kj"));
				final var kij = toLong(row.get("kij"));
				final var t1 = row.get("t1.name").toString();
				final var t2 = row.get("t2.name").toString();
				DBUtils.putSignificance(config, map, t1, t2, ki, kj, kij, k, kmax, function);
			}
			return map;
		}
	}

	private long countSentences(final Transaction tx) {
		final var stmt = " MATCH (:" + Labels.SENTENCE + ")\n" +
				"RETURN count(*)\n";
		try (final var result = tx.execute(stmt)) {
			final var row = result.next();
			return toLong(row.get("count(*)"));
		}
	}

	@Override
	public Map<String, Map<String, Double>> getDirectedSignificances(final WeightingFunction function) {
		final var stmt = " MATCH (t1:" + Labels.TERM + ")-[c:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n"
				+ "RETURN t1.name, t2.name, t1.count as ki, t2.count as kj, c.count as kij\n";
		try (final Transaction tx = graphDb.beginTx();
			 final var result = tx.execute(stmt)) {
			final var k = countSentences(tx);
			final var kmax = getMaxCount(tx);
			final var map = new TreeMap<String, Map<String, Double>>();
			while (result.hasNext()) {
				final var row = result.next();
				final var t1 = row.get("t1.name").toString();
				final var t2 = row.get("t2.name").toString();
				final var ki = toLong(row.get("ki"));
				final var kj = toLong(row.get("kj"));
				final var kij = toLong(row.get("kij"));
				DBUtils.putDirectedSignificance(config, map, t1, t2, ki, kj, kij, k, kmax, function);
			}
			return map;
		}
	}

	private long getMaxCount(final Transaction tx) {
		return config.isUseSentenceCount() ? getMaxSentencesCount(tx) : getMaxTermsCount(tx);
	}

	/**
	 * Returns the maximum number of sentences that contain the same term.
	 */
	private long getMaxSentencesCount(final Transaction tx) {
		final var stmt = " MATCH (s:" + Labels.SENTENCE + ")-[:" + RelationshipTypes.CONTAINS + "]-(t:" + Labels.TERM + ")\n"
				+ "  WITH t, count(distinct s) as c\n" // implicit group by t
				+ "RETURN max(c) as max\n";
		try (final var result = tx.execute(stmt)) {
			final var row = result.next();
			return toLong(ObjectUtils.defaultIfNull(row.get("max"), 0L));
		}
	}

	/**
	 * Returns the maximum number of occurrences of any term.
	 */
	private long getMaxTermsCount(final Transaction tx) {
		final var stmt = " MATCH (t:" + Labels.TERM + ")\n"
				+ "RETURN max(t.count) as max\n";
		try (final var result = tx.execute(stmt)) {
			final var row = result.next();
			return toLong(ObjectUtils.defaultIfNull(row.get("max"), 0L));
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
		final var stmt = " MATCH (d:" + Labels.DOCUMENT + ")-[:" + RelationshipTypes.CONTAINS + "*2]-(t:" + Labels.TERM + ")\n"
				+ "RETURN t.name, d.name, count(t)\n";
		try (final Transaction tx = graphDb.beginTx();
			 final var result = tx.execute(stmt)) {
			return result.stream()
					.collect(Collectors.groupingBy(row -> row.get("t.name").toString(),
							Collectors.toMap(row -> row.get("d.name").toString(), row -> toLong(row.get("count(t)")))
					));
		}
	}

	@Override
	public WeightedPath getShortestPath(final String start, final String end, final WeightingFunction function) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var k = countSentences(tx);
			final var kmax = getMaxCount(tx);
			final var evaluator = new SignificanceEvaluator(k, kmax, function);
			final var pathFinder = GraphAlgoFactory.dijkstra(PathExpanders.forType(RelationshipTypes.COOCCURS), evaluator, 1);
			final var path = pathFinder.findSinglePath(tx.findNode(Labels.TERM, "name", start), tx.findNode(Labels.TERM, "name", end));
			final var nodes = Utils.stream(path.nodes()).map(n -> n.getProperty("name").toString()).collect(Collectors.toList());
			return new WeightedPath(nodes, path.weight());
		}
	}

	@Override
	public List<List<String>> getAllSentencesInDocument(final String name) {
		final var stmt = "   MATCH (:" + Labels.DOCUMENT + " {name: $doc})-[r:" + RelationshipTypes.CONTAINS + "]-()-[p:" + RelationshipTypes.CONTAINS + "]-(t:" + Labels.TERM + ")\n"
				+ "  RETURN r.position, t.name\n"
				+ "ORDER BY r.position, p.position\n";
		final Map<String, Object> params = Map.of("doc", name);
		try (final Transaction tx = graphDb.beginTx();
			 final var result = tx.execute(stmt, params)) {
			// no Stream groupingBy to preserve the sentence/term order
			return collectToLists(result, "r.position", "t.name");
		}
	}

	private List<List<String>> collectToLists(final Result result, final String outerKey, final String innerKey) {
		final var lists = new ArrayList<List<String>>();
		Object currentSentence = null;
		while (result.hasNext()) {
			final var row = result.next();
			final var outer = row.get(outerKey);
			final var inner = row.get(innerKey).toString();
			if (!outer.equals(currentSentence)) {
				lists.add(new ArrayList<>());
				currentSentence = outer;
			}
			final var list = lists.get(lists.size() - 1);
			list.add(inner);
		}
		return lists;
	}

	@Override
	public boolean containsTerm(final String term) {
		try (final Transaction tx = graphDb.beginTx()) {
			return tx.findNode(Labels.TERM, "name", term) != null;
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

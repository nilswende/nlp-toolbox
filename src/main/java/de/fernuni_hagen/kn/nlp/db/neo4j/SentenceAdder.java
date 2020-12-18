package de.fernuni_hagen.kn.nlp.db.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Adds a sentence to the DB.
 *
 * @author Nils Wende
 */
class SentenceAdder {

	private final GraphDatabaseService graphDb;

	SentenceAdder(final GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
	}

	/**
	 * Delegate for {@link Neo4J#addSentence(List)}.
	 *
	 * @param terms        terms of a sentence
	 * @param currentDocId current document ID
	 */
	void addSentence(final List<String> terms, final long currentDocId) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var termNodes = addTermNodes(terms, tx);
			addTermRelationships(terms, tx);
			addSentenceNode(termNodes, currentDocId, tx);
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

	private void addSentenceNode(final List<Node> termNodes, final long currentDocId, final Transaction tx) {
		final var s = tx.createNode(Labels.SENTENCE);
		for (final Node term : termNodes) {
			final var r = s.createRelationshipTo(term, RelationshipTypes.CONTAINS);
			final var count = Utils.toLong(r.getProperty("count", 0)) + 1;
			r.setProperty("count", count);
		}
		final var doc = tx.findNode(Labels.DOCUMENT, "id", currentDocId);
		doc.createRelationshipTo(s, RelationshipTypes.CONTAINS);
	}

}

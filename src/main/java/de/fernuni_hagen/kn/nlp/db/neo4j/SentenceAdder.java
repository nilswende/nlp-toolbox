package de.fernuni_hagen.kn.nlp.db.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.Map;

/**
 * Adds a sentence to the DB.
 *
 * @author Nils Wende
 */
class SentenceAdder {

	private final GraphDatabaseService graphDb;
	private final Sequences sequences;

	SentenceAdder(final GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
		sequences = new Sequences(this.graphDb);
	}

	/**
	 * Delegate for {@link Neo4JWriter#addSentence(List)}.
	 *
	 * @param distinctTerms a sentence
	 * @param docId         document ID
	 */
	public void addSentence(final List<String> distinctTerms, final long docId) {
		try (final Transaction tx = graphDb.beginTx()) {
			addTermNodes(distinctTerms, tx);
			addTermRelationships(distinctTerms, tx);
			addSentenceNode(distinctTerms, docId, tx);
			tx.commit();
		}
	}

	private void addTermNodes(final List<String> terms, final Transaction tx) {
		final var stmt = "MERGE (t:" + Labels.TERM + " {name: $name})\n" +
				"ON CREATE SET t.name = $name, t.count = 1\n" +
				"ON  MATCH SET t.count = t.count + 1\n";
		terms.forEach(t -> addTerm(t, stmt, tx));
	}

	private void addTerm(final String term, final String stmt, final Transaction tx) {
		final Map<String, Object> params = Map.of("name", term);
		StatementPrinter.print(stmt, params);
		tx.execute(stmt, params);
	}

	private void addTermRelationships(final List<String> distinctTerms, final Transaction tx) {
		final var stmt = "MATCH (t1:" + Labels.TERM + " {name: $name1}),(t2:" + Labels.TERM + " {name: $name2})\n" +
				"MERGE (t1)-[r:" + RelationshipTypes.COOCCURS + "]-(t2)\n" +
				"ON CREATE SET r.count = 1\n" +
				"ON  MATCH SET r.count = r.count + 1\n";
		for (int i = 0; i < distinctTerms.size(); i++) {
			final var term1 = distinctTerms.get(i);
			for (int j = i + 1; j < distinctTerms.size(); j++) {
				final var term2 = distinctTerms.get(j);
				final Map<String, Object> params = Map.of("name1", term1, "name2", term2);
				StatementPrinter.print(stmt, params);
				tx.execute(stmt, params);
			}
		}
	}

	private void addSentenceNode(final List<String> terms, final long docId, final Transaction tx) {
		final long id = addSentenceNode(tx);
		addSentenceRelationships(id, terms, tx);
		addDocumentRelationship(id, docId, tx);
	}

	private long addSentenceNode(final Transaction tx) {
		final var id = sequences.nextValueFor(Labels.SENTENCE);
		final var node = tx.createNode(Labels.SENTENCE);
		node.setProperty("id", id);
		return id;
	}

	private void addSentenceRelationships(final long id, final List<String> terms, final Transaction tx) {
		final String stmt = "MATCH (s:" + Labels.SENTENCE + " {id: $id}),(t:" + Labels.TERM + " {name: $name})\n" +
				"MERGE (s)-[r:" + RelationshipTypes.CONTAINS + "]->(t)\n" +
				"ON CREATE SET r.count = 1\n" +
				"ON  MATCH SET r.count = r.count + 1\n";
		for (final String term : terms) {
			final Map<String, Object> params = Map.of("id", id, "name", term);
			StatementPrinter.print(stmt, params);
			tx.execute(stmt, params);
		}
	}

	private void addDocumentRelationship(final long id, final long docId, final Transaction tx) {
		final var stmt = " MATCH (s:" + Labels.SENTENCE + " {id: $sId}),(d:" + Labels.DOCUMENT + " {id: $dId})\n" +
				"CREATE (s)<-[r:" + RelationshipTypes.CONTAINS + "]-(d)\n";
		final Map<String, Object> params = Map.of("sId", id, "dId", docId);
		StatementPrinter.print(stmt, params);
		tx.execute(stmt, params);
	}

}

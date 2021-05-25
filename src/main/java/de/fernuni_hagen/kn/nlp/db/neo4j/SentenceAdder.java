package de.fernuni_hagen.kn.nlp.db.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	 * Delegate for {@link Neo4JWriter#addSentence(List)}.
	 *
	 * @param terms           a sentence
	 * @param docName         document name
	 * @param currentSentence current sentence position
	 */
	public void addSentence(final List<String> terms, final String docName, final long currentSentence) {
		try (final Transaction tx = graphDb.beginTx()) {
			final var termNodes = addTermNodes(terms, tx);
			addTermRelationships(termNodes);
			addSentenceNode(termNodes, docName, currentSentence, tx);
			tx.commit();
		}
	}

	private List<Node> addTermNodes(final List<String> terms, final Transaction tx) {
		final var stmt = "MERGE (t:" + Labels.TERM + " {name: $name})\n"
				+ "ON CREATE SET t.name = $name, t.count = 1\n"
				+ "ON  MATCH SET t.count = t.count + 1\n"
				+ "RETURN t\n";
		return terms.stream().map(t -> addTerm(t, stmt, tx)).collect(Collectors.toList());
	}

	private Node addTerm(final String term, final String stmt, final Transaction tx) {
		final Map<String, Object> params = Map.of("name", term);
		StatementPrinter.print(stmt, params);
		try (final var result = tx.execute(stmt, params)) {
			return ((Node) result.next().get("t"));
		}
	}

	private void addTermRelationships(final List<Node> termNodes) {
		for (int i = 0; i < termNodes.size(); i++) {
			final var term1 = termNodes.get(i);
			for (int j = i + 1; j < termNodes.size(); j++) {
				final var term2 = termNodes.get(j);
				if (!term1.getProperty("name").equals(term2.getProperty("name"))) {
					term1.createRelationshipTo(term2, RelationshipTypes.COOCCURS);
				}
			}
		}
	}

	private void addSentenceNode(final List<Node> termNodes, final String docName, final long currentSentence, final Transaction tx) {
		final var sentenceNode = tx.createNode(Labels.SENTENCE);
		addSentenceRelationships(sentenceNode, termNodes);
		addDocumentRelationship(sentenceNode, docName, currentSentence, tx);
	}

	private void addSentenceRelationships(final Node sentenceNode, final List<Node> terms) {
		for (int i = 0; i < terms.size(); i++) {
			final var termNode = terms.get(i);
			final var relationship = sentenceNode.createRelationshipTo(termNode, RelationshipTypes.CONTAINS);
			relationship.setProperty("position", i);
		}
	}

	private void addDocumentRelationship(final Node sentenceNode, final String docName, final long currentSentence, final Transaction tx) {
		final var docNode = tx.findNode(Labels.DOCUMENT, "name", docName);
		final var relationship = sentenceNode.createRelationshipTo(docNode, RelationshipTypes.CONTAINS);
		relationship.setProperty("position", currentSentence);
	}

}

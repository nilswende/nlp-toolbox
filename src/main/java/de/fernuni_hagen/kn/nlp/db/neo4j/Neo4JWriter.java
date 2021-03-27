package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.db.DBUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.nio.file.Path;
import java.util.List;

/**
 * Implements writing to the Neo4j graph database.
 *
 * @author Nils Wende
 */
public class Neo4JWriter implements DBWriter {

	private final GraphDatabaseService graphDb;
	private final Sequences sequences;
	private long currentDocId;

	public Neo4JWriter(final Neo4J db) {
		graphDb = db.getGraphDb();
		sequences = new Sequences(graphDb);
	}

	@Override
	public void deleteAll() {
		try (final Transaction tx = graphDb.beginTx()) {
			final var stmt = "MATCH (n)\n" +
					"DETACH DELETE n\n";
			tx.execute(stmt);
			tx.commit();
		}
	}

	@Override
	public void addDocument(final Path path) {
		final var name = DBUtils.normalizePath(path);
		try (final Transaction tx = graphDb.beginTx()) {
			if (tx.findNode(Labels.DOCUMENT, "name", name) != null) {
				System.out.println("no two input documents can have the same file name");
			}
			currentDocId = sequences.nextValueFor(Labels.DOCUMENT);
			final Node doc = tx.createNode(Labels.DOCUMENT);
			doc.setProperty("name", name);
			doc.setProperty("id", currentDocId);
			tx.commit();
		}
	}

	@Override
	public void addSentence(final List<String> terms) {
		new SentenceAdder(graphDb).addSentence(terms, currentDocId);
	}

}

package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.file.Exporter;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.List;

/**
 * Implements writing to the Neo4j graph database.
 *
 * @author Nils Wende
 */
public class Neo4JWriter implements DBWriter {

	private final Exporter exporter = new Exporter("data/output/neo4jWriter.txt", false);
	private final GraphDatabaseService graphDb;
	private String currentDocName;
	private long currentSentence;

	public Neo4JWriter(final Neo4J db) {
		graphDb = db.getGraphDb();
	}

	@Override
	public void deleteAll() {
		try (final Transaction tx = graphDb.beginTx()) {
			final var stmt = "MATCH (n)\n"
					+ "DETACH DELETE n\n";
			tx.execute(stmt);
			tx.commit();
		}
	}

	@Override
	public void addDocument(final String name) {
		try (final Transaction tx = graphDb.beginTx()) {
			if (tx.findNode(Labels.DOCUMENT, "name", name) != null) {
				throw new IllegalArgumentException("no two input documents can have the same file name");
			}
			final Node doc = tx.createNode(Labels.DOCUMENT);
			doc.setProperty("name", name);
			currentDocName = name;
			currentSentence = 0;
			tx.commit();
		}
	}

	@Override
	public void addSentence(final List<String> terms) {
		exporter.println(() -> String.join(StringUtils.SPACE, terms));
		new SentenceAdder(graphDb).addSentence(terms, currentDocName, currentSentence++);
	}

}

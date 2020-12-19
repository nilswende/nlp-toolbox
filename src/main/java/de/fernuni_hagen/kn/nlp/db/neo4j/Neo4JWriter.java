package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.math.WeightingFunctions;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.util.List;
import java.util.Map;

import static de.fernuni_hagen.kn.nlp.db.neo4j.Utils.toLong;

/**
 * Implements writing to the Neo4j graph database.
 *
 * @author Nils Wende
 */
public class Neo4JWriter implements DBWriter {

	private final GraphDatabaseService graphDb = Neo4J.instance().getGraphDb();
	private final Sequences sequences = new Sequences(graphDb);
	private long currentDocId;

	public void deleteAll() {
		try (final Transaction tx = graphDb.beginTx()) {
			final var stmt = "MATCH (n)\n" +
					"DETACH DELETE n\n";
			tx.execute(stmt);
			tx.commit();
		}
	}

	@Override
	public void addDocument(final File file) {
		currentDocId = sequences.nextValueFor(Labels.DOCUMENT);
		try (final Transaction tx = graphDb.beginTx()) {
			final Node doc = tx.createNode(Labels.DOCUMENT);
			doc.setProperty("name", file.getName());
			doc.setProperty("id", currentDocId);
			tx.commit();
		}
	}

	@Override
	public void addSentence(final List<String> terms) {
		new SentenceAdder(graphDb).addSentence(terms, currentDocId);
	}

	@Override
	public void updateDiceAndCosts() {
		final var stmt = "MATCH (t1:" + Labels.TERM + ")-[r:" + RelationshipTypes.COOCCURS + "]-(t2:" + Labels.TERM + ")\n" +
				"RETURN t1.count, t2.count, r.count, r";
		try (final Transaction tx = graphDb.beginTx();
			 final var result = tx.execute(stmt)) {
			while (result.hasNext()) {
				final var row = result.next();
				final var dice = calculateDice(row);
				final var relationship = (Relationship) row.get("r");
				relationship.setProperty("dice", dice);
				relationship.setProperty("cost", 1 / (dice + 0.01));
			}
			tx.commit();
		}
	}

	private double calculateDice(final Map<String, Object> row) {
		final var a = toLong(row.get("t1.count"));
		final var b = toLong(row.get("t2.count"));
		var ab = toLong(row.get("r.count"));
		ab = Math.min(ab, Math.min(a, b));
		return Math.min(1, WeightingFunctions.DICE.calculate(a, b, ab, 0));
	}

}

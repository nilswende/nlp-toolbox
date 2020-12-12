package de.fernuni_hagen.kn.nlp.db.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;

import java.util.Map;

import static de.fernuni_hagen.kn.nlp.db.neo4j.Utils.toLong;

/**
 * Emulates a DB sequence by merging on nodes of a special label.
 * This allows concurrent access as well as reuse of the DB over multiple application starts.
 *
 * @author Nils Wende
 */
class Sequences {

	private final GraphDatabaseService graphDb;

	Sequences(final GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
	}

	/**
	 * Returns the next sequence value for the given label.
	 *
	 * @param label Label
	 * @return the next sequence value, starting with 1
	 */
	public long nextValueFor(final Label label) {
		final var stmt = "MERGE (s:" + Labels.SEQUENCE + " {name: $name})\n" +
				"ON CREATE SET s.name = $name, s.value = 1\n" +
				"ON  MATCH SET s.value = s.value + 1\n" +
				"RETURN s.value\n";
		try (final Transaction tx = graphDb.beginTx();
			 final var result = tx.execute(stmt, Map.of("name", label.name()))) {
			final var value = result.next().get("s.value");
			tx.commit();
			return toLong(value);
		}
	}

}

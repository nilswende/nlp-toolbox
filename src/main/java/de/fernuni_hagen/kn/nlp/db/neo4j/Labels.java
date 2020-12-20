package de.fernuni_hagen.kn.nlp.db.neo4j;

import org.neo4j.graphdb.Label;

/**
 * Different node groups.
 *
 * @author Nils Wende
 */
enum Labels implements Label {
	TERM, SENTENCE, DOCUMENT, // functional
	SEQUENCE // technical
	;

	private final String displayName = name().charAt(0) + name().substring(1).toLowerCase();

	@Override
	public String toString() {
		return displayName;
	}
}

package de.fernuni_hagen.kn.nlp.db.neo4j;

import org.neo4j.graphdb.Label;

/**
 * Different node groups.
 *
 * @author Nils Wende
 */
enum Labels implements Label {
	TERM, SENTENCE, DOCUMENT
}

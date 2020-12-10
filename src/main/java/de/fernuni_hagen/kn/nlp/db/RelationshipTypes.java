package de.fernuni_hagen.kn.nlp.db;

import org.neo4j.graphdb.RelationshipType;

/**
 * The type of a relationship between nodes.
 *
 * @author Nils Wende
 */
enum RelationshipTypes implements RelationshipType {
	CONNECTED
}

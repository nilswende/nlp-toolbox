package de.fernuni_hagen.kn.nlp.db.neo4j;

import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

public class EntityFormatter {

	public static String formatNode(final Node n) {
		return "(:" + n.getLabels().iterator().next() + " " + formatEntity(n) + ")";
	}

	public static String formatEntity(final Entity e) {
		return e.getAllProperties().toString();
	}

	public static String formatRelationship(final Relationship r) {
		return formatNode(r.getStartNode()) + "-[:" + formatEntity(r) + "]-" + formatNode(r.getEndNode());
	}

	public static String formatPath(final Path p) {
		final var sb = new StringBuilder();
		for (Relationship relationship : p.relationships()) {
			sb.append(formatRelationship(relationship));
			sb.append('\n');
		}
		return sb.toString();
	}

}

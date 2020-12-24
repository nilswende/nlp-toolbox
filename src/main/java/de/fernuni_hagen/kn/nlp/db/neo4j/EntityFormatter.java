package de.fernuni_hagen.kn.nlp.db.neo4j;

import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
		return StreamSupport.stream(p.relationships().spliterator(), false)
				.map(EntityFormatter::formatRelationship)
				.collect(Collectors.joining("\n"));
	}

}

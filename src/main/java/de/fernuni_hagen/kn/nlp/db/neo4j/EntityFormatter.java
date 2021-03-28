package de.fernuni_hagen.kn.nlp.db.neo4j;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

import java.util.stream.Collectors;

/**
 * Formats Neo4j entities for printing.
 *
 * @author Nils Wende
 */
class EntityFormatter {

	/**
	 * Formats a Node.
	 *
	 * @param n Node
	 * @return formatted Node
	 */
	public static String formatNode(final Node n) {
		return "(:" + n.getLabels().iterator().next() + formatEntity(n) + ")";
	}

	private static String formatEntity(final Entity e) {
		final var allProperties = e.getAllProperties();
		return allProperties.isEmpty() ? StringUtils.EMPTY : " " + allProperties.toString();
	}

	/**
	 * Formats a Relationship.
	 *
	 * @param r Relationship
	 * @return formatted Relationship
	 */
	public static String formatRelationship(final Relationship r) {
		return formatNode(r.getStartNode()) + "-[:" + r.getType() + formatEntity(r) + "]-" + formatNode(r.getEndNode());
	}

	/**
	 * Formats a Path.
	 *
	 * @param p Path
	 * @return formatted Path
	 */
	public static String formatPath(final Path p) {
		return Neo4JUtils.stream(p.relationships())
				.map(EntityFormatter::formatRelationship)
				.collect(Collectors.joining("\n"));
	}

}

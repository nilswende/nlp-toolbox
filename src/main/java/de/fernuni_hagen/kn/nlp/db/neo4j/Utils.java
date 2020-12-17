package de.fernuni_hagen.kn.nlp.db.neo4j;

/**
 * Neo4J utilities.
 *
 * @author Nils Wende
 */
final class Utils {

	private Utils() {
		throw new AssertionError(); // no init
	}

	/**
	 * Converts an Object to long.
	 *
	 * @param o Object
	 * @return long
	 */
	public static long toLong(final Object o) {
		return ((Number) o).longValue();
	}

}
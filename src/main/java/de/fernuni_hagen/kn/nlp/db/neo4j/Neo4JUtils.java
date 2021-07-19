package de.fernuni_hagen.kn.nlp.db.neo4j;

/**
 * Neo4J utilities.
 *
 * @author Nils Wende
 */
final class Neo4JUtils {

	private Neo4JUtils() {
		// no init
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

	/**
	 * Converts an Object to double.
	 *
	 * @param o Object
	 * @return double
	 */
	public static double toDouble(final Object o) {
		return ((Number) o).doubleValue();
	}

}

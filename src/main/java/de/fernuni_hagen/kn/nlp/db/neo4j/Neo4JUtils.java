package de.fernuni_hagen.kn.nlp.db.neo4j;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Neo4J utilities.
 *
 * @author Nils Wende
 */
final class Neo4JUtils {

	private Neo4JUtils() {
		throw new AssertionError("no init");
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

	/**
	 * Creates a Stream from an Iterable.
	 *
	 * @param it  an Iterable
	 * @param <T> the type of elements returned by the iterator
	 * @return a Stream
	 */
	public static <T> Stream<T> stream(final Iterable<T> it) {
		return StreamSupport.stream(it.spliterator(), false);
	}

}

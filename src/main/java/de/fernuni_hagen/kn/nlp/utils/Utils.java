package de.fernuni_hagen.kn.nlp.utils;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Common utilities.
 *
 * @author Nils Wende
 */
public final class Utils {

	private Utils() {
		throw new AssertionError("no init");
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

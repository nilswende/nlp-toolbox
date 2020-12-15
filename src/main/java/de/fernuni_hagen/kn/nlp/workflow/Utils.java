package de.fernuni_hagen.kn.nlp.workflow;

import java.util.List;

/**
 * Utilities.
 *
 * @author Nils Wende
 */
public final class Utils {

	private Utils() {
		throw new AssertionError(); // no init
	}

	/**
	 * Casts a raw List into a parameterized List.
	 *
	 * @param arg the raw List
	 * @param <E> the type of elements in the list
	 * @return a parameterized List
	 */
	@SuppressWarnings("unchecked")
	public static <E> List<E> cast(@SuppressWarnings("rawtypes") final List arg) {
		return (List<E>) arg;
	}

}

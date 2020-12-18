package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.List;
import java.util.Set;

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

	/**
	 * Casts a raw Set into a parameterized Set.
	 *
	 * @param arg the raw Set
	 * @param <E> the type of elements in the Set
	 * @return a parameterized Set
	 */
	@SuppressWarnings("unchecked")
	public static <E> Set<E> cast(@SuppressWarnings("rawtypes") final Set arg) {
		return (Set<E>) arg;
	}

}

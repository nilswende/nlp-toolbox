package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities.
 *
 * @author Nils Wende
 */
public final class PreprocessingUtils {

	private PreprocessingUtils() {
		throw new AssertionError("no init");
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

	/**
	 * Casts a raw Map into a parameterized Map.
	 *
	 * @param arg the raw Map
	 * @param <K> the type of keys in the Map
	 * @param <V> the type of values in the Map
	 * @return a parameterized Map
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> cast(@SuppressWarnings("rawtypes") final Map arg) {
		return (Map<K, V>) arg;
	}

}

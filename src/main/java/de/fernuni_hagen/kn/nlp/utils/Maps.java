package de.fernuni_hagen.kn.nlp.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Map utilities.
 *
 * @author Nils Wende
 */
public final class Maps {

	private Maps() {
		throw new AssertionError("no init");
	}

	/**
	 * Creates a new HashMap with an initial capacity high enough to hold {@code size} entries without rehashing.
	 *
	 * @param size the expected number of entries of the new HashMap
	 * @param <K>  key type
	 * @param <V>  value type
	 * @return a new HashMap
	 */
	public static <K, V> HashMap<K, V> newKnownSizeMap(final int size) {
		return new HashMap<>((int) (size / 0.75) + 1);
	}

	/**
	 * Inverts the mapping {@code Map<K, Map<K2, V>>} to {@code Map<K2, Map<K, V>>}.
	 *
	 * @param map  the map
	 * @param <K>  first key type
	 * @param <K2> second key type
	 * @param <V>  value type
	 * @return an inverted map
	 */
	public static <K, K2, V> Map<K2, Map<K, V>> invertMapping(final Map<K, Map<K2, V>> map) {
		final var inverted = new HashMap<K2, Map<K, V>>();
		map.forEach(
				(k, m) -> m.forEach(
						(k2, v) -> inverted.computeIfAbsent(k2, x -> newKnownSizeMap(map.size())).put(k, v)
				));
		return inverted;
	}

}

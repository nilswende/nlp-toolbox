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
	 * @apiNote Should only be used when you need to explicitly create a new map and you know its size beforehand.
	 * If you can create the map by using e. g. Collectors.toMap, do so, since it is usually clearer and more concise.
	 */
	public static <K, V> HashMap<K, V> newKnownSizeMap(final int size) {
		return new HashMap<>((int) (size / 0.75) + 1);
	}

	/**
	 * Inverts the mapping {@code Map<K1, Map<K2, V>>} to {@code Map<K2, Map<K1, V>>}.
	 *
	 * @param map  the map
	 * @param <K1> first key type
	 * @param <K2> second key type
	 * @param <V>  value type
	 * @return the inverted map
	 */
	public static <K1, K2, V> Map<K2, Map<K1, V>> invertMapping(final Map<K1, Map<K2, V>> map) {
		final var inverted = new HashMap<K2, Map<K1, V>>();
		map.forEach(
				(k1, m) -> m.forEach(
						(k2, v) -> inverted.computeIfAbsent(k2, x -> newKnownSizeMap(map.size())).put(k1, v)
				));
		return inverted;
	}

	/**
	 * Returns a mutable copy of the given map.
	 *
	 * @param map the map
	 * @return the mutable copy
	 */
	public static Map<String, Map<String, Double>> copyOf(final Map<String, Map<String, Double>> map) {
		final var copy = new HashMap<>(map);
		copy.replaceAll((k, v) -> new HashMap<>(v));
		return copy;
	}

}

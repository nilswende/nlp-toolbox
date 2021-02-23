package de.fernuni_hagen.kn.nlp.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	public static <K, V> HashMap<K, V> newHashMap(final int size) {
		return new HashMap<>((int) (size / 0.75) + 1);
	}

	/**
	 * Returns all map keys.
	 *
	 * @param <K> key type
	 * @param <V> value type
	 * @param map the map
	 * @return all outer and inner keys
	 */
	public static <K, V> Set<K> getKeys(final Map<K, Map<K, V>> map) {
		final var keys = new HashSet<>(map.keySet());
		map.values().stream().map(Map::keySet).forEach(keys::addAll);
		return keys;
	}

	/**
	 * Returns all inner map keys.
	 *
	 * @param <K> inner key type
	 * @param <V> value type
	 * @param map the map
	 * @return all inner keys
	 */
	public static <K, V> Set<K> getInnerKeys(final Map<?, Map<K, V>> map) {
		final var keys = new HashSet<K>();
		map.values().stream().map(Map::keySet).forEach(keys::addAll);
		return keys;
	}

	/**
	 * Returns a mutable copy of the given map.
	 *
	 * @param map  the map
	 * @param <K1> first key type
	 * @param <K2> second key type
	 * @param <V>  value type
	 * @return the mutable copy
	 */
	public static <K1, K2, V> Map<K1, Map<K2, V>> copyOf(final Map<K1, Map<K2, V>> map) {
		final var copy = new HashMap<>(map);
		copy.replaceAll((k, v) -> new HashMap<>(v));
		return copy;
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
						(k2, v) -> inverted.computeIfAbsent(k2, x -> newHashMap(map.size())).put(k1, v)
				));
		return inverted;
	}

	/**
	 * Inverts the values of the map.
	 *
	 * @param map  the map with each value != 0
	 * @param <K1> first key type
	 * @param <K2> second key type
	 * @return the same map now containing the multiplicative inverse of its former values
	 */
	public static <K1, K2> Map<K1, Map<K2, Double>> invertValues(final Map<K1, Map<K2, Double>> map) {
		map.forEach((k1, m) -> m.replaceAll((k2, v) -> 1 / v));
		return map;
	}

	/**
	 * Transforms the values of the map by applying the mapper function to each element.
	 *
	 * @param map         the map
	 * @param mapMapper   creates a value from the inner map
	 * @param valueMapper creates a value from each element and its inner map value
	 * @param <K1>        first key type
	 * @param <K2>        second key type
	 * @param <V>         value type
	 * @return the same map
	 */
	public static <K1, K2, V> Map<K1, Map<K2, V>> transform(
			final Map<K1, Map<K2, V>> map,
			final Function<Map<K2, V>, V> mapMapper,
			final BiFunction<V, V, V> valueMapper) {
		map.forEach((k1, m) -> {
			final var v1 = mapMapper.apply(m);
			m.replaceAll((k2, v) -> valueMapper.apply(v1, v));
		});
		return map;
	}

	/**
	 * Transforms the map containing Longs to a new map containing Doubles by applying the mapper function to each element.
	 *
	 * @param map    Long map
	 * @param mapper transformation function
	 * @param <K1>   first key type
	 * @param <K2>   second key type
	 * @return a Double map
	 */
	public static <K1, K2> Map<K1, Map<K2, Double>> transformCopy(
			final Map<K1, Map<K2, Long>> map,
			final Function<Long, Double> mapper) {
		return transformCopy(map, m -> Double.NaN, (x, l) -> mapper.apply(l));
	}

	/**
	 * Transforms the map containing V1 values to a new map containing V2 values by applying the mapper functions to each element.
	 *
	 * @param map         V1 map
	 * @param mapMapper   creates a V2 value from the inner map
	 * @param valueMapper creates a V2 value from each V1 and its inner map value
	 * @param <K1>        first key type
	 * @param <K2>        second key type
	 * @param <V1>        first value type
	 * @param <V2>        second value type
	 * @return a new V2 valued map
	 */
	public static <K1, K2, V1, V2> Map<K1, Map<K2, V2>> transformCopy(
			final Map<K1, Map<K2, V1>> map,
			final Function<Map<K2, V1>, V2> mapMapper,
			final BiFunction<V2, V1, V2> valueMapper) {
		final var copy = Maps.<K1, Map<K2, V2>>newHashMap(map.size());
		map.forEach((k, v) -> {
			final var inner = v.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey,
							e -> valueMapper.apply(mapMapper.apply(v), e.getValue())));
			copy.put(k, inner);
		});
		return copy;
	}

}

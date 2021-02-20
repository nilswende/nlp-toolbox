package de.fernuni_hagen.kn.nlp.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	 * Returns the distinct count of all inner map keys.
	 *
	 * @param map  the map
	 * @param <K1> first key type
	 * @param <K2> second key type
	 * @param <V>  value type
	 * @return inner key count
	 */
	public static <K1, K2, V> int getInnerKeyCount(final Map<K1, Map<K2, V>> map) {
		return (int) streamInnerKeys(map).count();
	}

	/**
	 * Returns all inner map keys.
	 *
	 * @param map  the map
	 * @param <K1> first key type
	 * @param <K2> second key type
	 * @param <V>  value type
	 * @return inner keys
	 */
	public static <K1, K2, V> List<K2> getInnerKeys(final Map<K1, Map<K2, V>> map) {
		return streamInnerKeys(map).collect(Collectors.toList());
	}

	private static <K1, K2, V> Stream<K2> streamInnerKeys(final Map<K1, Map<K2, V>> map) {
		return map.values().stream()
				.flatMap(inner -> inner.keySet().stream())
				.distinct();
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
	 * Transforms the map containing Longs to one containing Doubles by applying the mapper function to each element.
	 *
	 * @param map    Long map
	 * @param mapper transformation function
	 * @param <K>    key type
	 * @return Double map
	 */
	public static <K> Map<K, Map<K, Double>> transform(
			final Map<K, Map<K, Long>> map,
			final Function<Long, Double> mapper) {
		return transform(map, m -> Double.NaN, (x, l) -> mapper.apply(l));
	}

	/**
	 * Transforms the map containing V1 to one containing V2 by applying the mapper functions to each element.
	 *
	 * @param map         V1 map
	 * @param mapMapper   creates a V2 value from the inner map
	 * @param valueMapper creates a V2 value from each V1 and its inner map value
	 * @param <K>         key type
	 * @param <V1>        first value type
	 * @param <V2>        second value type
	 * @return V2 map
	 */
	public static <K, V1, V2> Map<K, Map<K, V2>> transform(
			final Map<K, Map<K, V1>> map,
			final Function<Map<K, V1>, V2> mapMapper,
			final BiFunction<V2, V1, V2> valueMapper) {
		final var copy = Maps.<K, Map<K, V2>>newKnownSizeMap(map.size());
		map.forEach((k, v) -> {
			final var inner = v.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey,
							e -> valueMapper.apply(mapMapper.apply(v), e.getValue())));
			copy.put(k, inner);
		});
		return copy;
	}

}

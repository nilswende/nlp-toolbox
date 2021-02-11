package de.fernuni_hagen.kn.nlp.utils;

import java.util.HashMap;

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
	 * @param <K>  the type of keys maintained by this map
	 * @param <V>  the type of mapped values
	 * @return a new HashMap
	 */
	public static <K, V> HashMap<K, V> newKnownSizeMap(final int size) {
		return new HashMap<>((int) (size / 0.75) + 1);
	}

}

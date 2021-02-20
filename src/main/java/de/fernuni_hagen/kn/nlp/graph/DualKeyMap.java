package de.fernuni_hagen.kn.nlp.graph;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Nils Wende
 */
public class DualKeyMap<K, V> extends AbstractMap<K, V> {

	private final Map<K, Map<K, V>> m1 = new HashMap<>();
	private final Map<K, Map<K, V>> m2 = new HashMap<>();

	public V put(final K k1, final K k2, final V v) {
		m1.computeIfAbsent(k1, x -> new HashMap<>()).put(k2, v);
		return m2.computeIfAbsent(k2, x -> new HashMap<>()).put(k1, v);
	}

	public boolean containsKey(final K k1, final K k2) {
		return m1.containsKey(k1) && m1.get(k1).containsKey(k2);
	}

	public Set<K> getFirstKeys(final K k2) {
		return m2.get(k2).keySet();
	}

	public Set<K> getSecondKeys(final K k1) {
		return m1.get(k1).keySet();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return null;
	}
}

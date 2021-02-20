package de.fernuni_hagen.kn.nlp.graph;

import org.apache.commons.collections4.map.MultiKeyMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Nils Wende
 */
public class DualKeyMap3<K, V> extends MultiKeyMap<K, V> {

	private final Map<K, Set<K>> m1 = new HashMap<>();
	private final Map<K, Set<K>> m2 = new HashMap<>();

	public V put(final K k1, final K k2, final V v) {
		m1.computeIfAbsent(k1, x -> new HashSet<>()).add(k2);
		m2.computeIfAbsent(k2, x -> new HashSet<>()).add(k1);
		return super.put(k1, k2, v);
	}

	public Set<K> getFirstKeys() {
		return m1.keySet();
	}

	public Set<K> getSecondKeys() {
		return m2.keySet();
	}

	public Set<K> getFirstKeys(final K k2) {
		return m2.get(k2);
	}

	public Set<K> getSecondKeys(final K k1) {
		return m1.get(k1);
	}

}

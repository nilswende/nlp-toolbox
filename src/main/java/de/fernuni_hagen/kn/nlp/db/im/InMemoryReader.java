package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.math.DirectedWeightingFunctions;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implements reading from the in-memory database.
 *
 * @author Nils Wende
 */
public class InMemoryReader implements DBReader {

	@Override
	public Map<String, List<String>> getCooccurrences() {
		return null;
	}

	@Override
	public Map<String, Map<String, Double>> getSignificances(final WeightingFunction function) {
		final var map = new TreeMap<String, Map<String, Double>>();
		final var data = InMemoryDB.INSTANCE.getData();
		for (final Map.Entry<String, InMemoryDB.Values> entry : data.entrySet()) {
			final var ki = entry.getValue().getCount();
			final var cooccs = entry.getValue().getCooccs();
			final var inner = new TreeMap<String, Double>();
			for (final Map.Entry<String, Long> coocc : cooccs.entrySet()) {
				final var kj = data.get(coocc.getKey()).getCount();
				final var kij = coocc.getValue();
				final var sig = function.calculate(ki, kj, kij, data.size());
				inner.put(coocc.getKey(), sig);
			}
			map.put(entry.getKey(), inner);
		}
		return map;
	}

	@Override
	public Map<String, Map<String, Double>> getSignificances(final DirectedWeightingFunctions function) {
		final var map = new TreeMap<String, Map<String, Double>>();
		final var db = InMemoryDB.INSTANCE;
		final var data = db.getData();
		for (final Map.Entry<String, InMemoryDB.Values> entry : data.entrySet()) {
			final var ki = entry.getValue().getCount();
			final var cooccs = entry.getValue().getCooccs();
			for (final Map.Entry<String, Long> coocc : cooccs.entrySet()) {
				final var kj = data.get(coocc.getKey()).getCount();
				final var kij = coocc.getValue();
				final var dom = ki > kj ? entry.getKey() : coocc.getKey();
				final var sub = ki > kj ? coocc.getKey() : entry.getKey();
				if (!map.containsKey(sub) || !map.get(sub).containsKey(dom)) {
					final var sig = function.calculate(kij, db.getMaxSentencesCount());
					map.computeIfAbsent(dom, k -> new TreeMap<>()).put(sub, sig);
				}
			}
		}
		return map;
	}

}

package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.math.DirectedWeightingFunction;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Implements reading from the in-memory database.
 *
 * @author Nils Wende
 */
public class InMemoryReader implements DBReader {

	private final InMemoryDB db;

	public InMemoryReader(final InMemoryDB db) {
		this.db = db;
	}

	@Override
	public Map<String, List<String>> getCooccurrences() {
		return null;
	}

	@Override
	public Map<String, Map<String, Double>> getSignificances(final WeightingFunction function) {
		final var data = db.getData();
		final var map = Maps.<String, Map<String, Double>>newKnownSizeMap(data.size());
		for (final Map.Entry<String, InMemoryDB.Values> entry : data.entrySet()) {
			final var ki = entry.getValue().getCount();
			final var cooccs = entry.getValue().getCooccs();
			final var inner = Maps.<String, Double>newKnownSizeMap(cooccs.size());
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
	public Map<String, Map<String, Double>> getSignificances(final DirectedWeightingFunction function) {
		final var data = db.getData();
		final var kmax = db.getMaxSentencesCount();
		final var map = Maps.<String, Map<String, Double>>newKnownSizeMap(data.size());
		for (final Map.Entry<String, InMemoryDB.Values> entry : data.entrySet()) {
			final var ki = entry.getValue().getCount();
			final var cooccs = entry.getValue().getCooccs();
			for (final Map.Entry<String, Long> coocc : cooccs.entrySet()) {
				final var kj = data.get(coocc.getKey()).getCount();
				final var kij = coocc.getValue();
				final var dom = ki > kj ? entry.getKey() : coocc.getKey();
				final var sub = ki > kj ? coocc.getKey() : entry.getKey();
				if (!map.containsKey(sub) || !map.get(sub).containsKey(dom)) {
					final var sig = function.calculate(kij, kmax);
					map.computeIfAbsent(dom, k -> new TreeMap<>()).put(sub, sig);
				}
			}
		}
		return map;
	}

	/**
	 * Gets all terms in the given document.
	 *
	 * @param path original path of a preprocessed file
	 * @return all terms in the given document
	 */
	public List<String> getAllTermsInDocument(final Path path) {
		final var pathStr = InMemoryDB.formatPath(path);
		return db.getData().entrySet().stream()
				.filter(e -> e.getValue().getDocuments().contains(pathStr))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

}

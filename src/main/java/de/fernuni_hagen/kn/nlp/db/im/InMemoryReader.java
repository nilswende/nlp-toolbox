package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.db.DBUtils;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.nio.file.Path;
import java.util.HashMap;
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
	public Map<String, Map<String, Double>> getCooccurrences() {
		final var data = db.getData();
		final var copy = Maps.<String, Map<String, Double>>newKnownSizeMap(data.size());
		data.forEach((k, v) -> {
			final var cooccs = v.getCooccs()
					.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey,
							e -> e.getValue().doubleValue()));
			copy.put(k, cooccs);
		});
		return copy;
	}

	@Override
	public Map<String, Map<String, Double>> getSignificances(final WeightingFunction function) {
		final var k = db.getSentencesCount();
		final var kmax = db.getMaxSentencesCount();
		final var data = db.getData();
		final var cooccs = getCooccurrences();
		cooccs.forEach((ti, m) -> {
			final var ki = data.get(ti).getCount();
			m.replaceAll((tj, v) -> {
				final var kj = data.get(tj).getCount();
				final var kij = v.longValue();
				return function.calculate(ki, kj, kij, k, kmax);
			});
		});
		return cooccs;
	}

	@Override
	public Map<String, Map<String, Double>> getDirectedSignificances(WeightingFunction function) {
		final var k = db.getSentencesCount();
		final var kmax = db.getMaxSentencesCount();
		final var data = db.getData();
		final var cooccs = Maps.<String, Map<String, Double>>newKnownSizeMap(data.size());
		data.forEach((ti, m) -> {
			final var ki = m.getCount();
			m.getCooccs().forEach((tj, kij) -> {
				final var kj = data.get(tj).getCount();
				final var dom = ki > kj ? ti : tj;
				final var sub = ki > kj ? tj : ti;
				if (!cooccs.containsKey(sub) || !cooccs.get(sub).containsKey(dom)) {
					final var sig = function.calculate(ki, kj, kij, k, kmax);
					cooccs.computeIfAbsent(dom, x -> new TreeMap<>()).put(sub, sig);
				}
			});
		});
		return cooccs;
	}

	@Override
	public Map<String, Map<String, Long>> getTermFrequencies() {
		final var data = db.getData();
		final var copy = Maps.<String, Map<String, Long>>newKnownSizeMap(data.size());
		data.forEach((k, v) -> copy.put(k, new HashMap<>(v.getDocuments())));
		return copy;
	}

	/**
	 * Gets all terms in the given document.
	 *
	 * @param path original path of a preprocessed file
	 * @return all terms in the given document
	 */
	public List<String> getAllTermsInDocument(final Path path) {
		final var pathStr = DBUtils.normalizePath(path);
		return db.getData().entrySet().stream()
				.filter(e -> e.getValue().getDocuments().containsKey(pathStr))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

}

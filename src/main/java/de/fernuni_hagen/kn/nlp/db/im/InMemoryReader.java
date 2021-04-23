package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.graph.DijkstraSearcher;
import de.fernuni_hagen.kn.nlp.graph.WeightedPath;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.ArrayList;
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
		final var copy = Maps.<String, Map<String, Double>>newHashMap(data.size());
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
		final var kmax = db.getMaxTermCount();
		final var data = db.getData();
		final var cooccs = Maps.<String, Map<String, Double>>newHashMap(data.size());
		data.forEach((ti, m) -> {
			final var ki = m.getSentenceCount();
			m.getCooccs().forEach((tj, kij) -> {
				final var kj = data.get(tj).getSentenceCount();
				double sig = 0;
				if (ki > 1 || kj > 1) {
					sig = function.calculate(ki, kj, kij, k, kmax);
				}
				cooccs.computeIfAbsent(ti, x -> new TreeMap<>()).put(tj, sig);
			});
		});
		return cooccs;
	}

	@Override
	public Map<String, Map<String, Double>> getDirectedSignificances(final WeightingFunction function) {
		final var k = db.getSentencesCount();
		final var kmax = db.getMaxTermCount();
		final var data = db.getData();
		final var cooccs = Maps.<String, Map<String, Double>>newHashMap(data.size());
		data.forEach((ti, m) -> {
			final var ki = m.getSentenceCount();
			m.getCooccs().forEach((tj, kij) -> {
				final var kj = data.get(tj).getSentenceCount();
				// use the direction with the higher significance
				var sig = 0.01;
				if ((ki > 1 || kj > 1) && kj >= ki) {
					sig = function.calculate(ki, kj, kij, k, kmax);
				}
				cooccs.computeIfAbsent(ti, x -> new TreeMap<>()).put(tj, sig);
			});
		});
		return cooccs;
	}

	@Override
	public Map<String, Map<String, Long>> getTermFrequencies() {
		final var map = new HashMap<String, Map<String, Long>>();
		db.getData().forEach(
				(k, v) -> v.getDocuments().forEach(
						(d, n) -> map.computeIfAbsent(k, x -> new HashMap<>()).put(d, n)
				));
		return map;
	}

	@Override
	public WeightedPath getShortestPath(final String start, final String end, final WeightingFunction function) {
		final var distances = Maps.invertValues(getSignificances(function));
		return new DijkstraSearcher().search(start, end, distances);
	}

	/**
	 * Gets all terms in the given document.
	 *
	 * @param name original name of a preprocessed document
	 * @return all terms in the given document
	 */
	public List<String> getAllTermsInDocument(final String name) {
		return getAllSentencesInDocument(name).stream()
				.flatMap(List::stream)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public List<List<String>> getAllSentencesInDocument(final String name) {
		return db.getDoc2Sentences().get(name).stream().map(ArrayList::new).collect(Collectors.toList());
	}

}

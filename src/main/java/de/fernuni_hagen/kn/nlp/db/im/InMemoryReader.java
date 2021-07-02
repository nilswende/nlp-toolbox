package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.DBUtils;
import de.fernuni_hagen.kn.nlp.graph.DijkstraSearcher;
import de.fernuni_hagen.kn.nlp.graph.WeightedPath;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements reading from the in-memory database.
 *
 * @author Nils Wende
 */
public class InMemoryReader implements DBReader {

	private final AppConfig config;
	private final InMemoryDB db;

	public InMemoryReader(final AppConfig config, final InMemoryDB db) {
		this.config = config;
		this.db = db;
	}

	@Override
	public Map<String, Map<String, Double>> getCooccurrences() {
		final var data = db.getData();
		final var copy = Maps.<String, Map<String, Double>>newHashMap(data.size());
		data.forEach((k, v) -> copy.put(k, v.getCooccsAsDouble()));
		return copy;
	}

	@Override
	public Map<String, Double> getCooccurrences(final String term) {
		final var values = db.getData().get(term);
		return values == null ? Map.of() : values.getCooccsAsDouble();
	}

	@Override
	public Map<String, Map<String, Double>> getSignificances(final WeightingFunction function) {
		final var k = db.getSentencesCount();
		final var kmax = db.getMaxTermCount();
		final var data = db.getData();
		final var cooccs = Maps.<String, Map<String, Double>>newHashMap(data.size());
		data.forEach((ti, m) -> {
			final var ki = m.getCount();
			m.getCooccs().forEach((tj, kij) -> {
				final var kj = data.get(tj).getCount();
				DBUtils.putSignificance(config, cooccs, ti, tj, ki, kj, kij, k, kmax, function);
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
			final var ki = m.getCount();
			m.getCooccs().forEach((tj, kij) -> {
				final var kj = data.get(tj).getCount();
				DBUtils.putDirectedSignificance(config, cooccs, ti, tj, ki, kj, kij, k, kmax, function);
			});
		});
		return cooccs;
	}

	@Override
	public Map<String, Map<String, Long>> getTermFrequencies() {
		return db.getData().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getDocuments()));
	}

	/**
	 * Gets the shortest path between the two nodes.<br>
	 * For repeated invocations, it is more efficient to save the distances map to your use case and find the shortest paths with that.
	 */
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

	public List<String> getAllNodes() {
		return List.copyOf(db.getData().keySet());
	}

	@Override
	public boolean containsTerm(final String term) {
		return db.getData().containsKey(term);
	}

}

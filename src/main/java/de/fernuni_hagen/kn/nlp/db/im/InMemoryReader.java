package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.db.DBUtils;
import de.fernuni_hagen.kn.nlp.math.DirectedWeightingFunction;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
	public MultiKeyMap<String, Double> getCooccurrences() {
		final var map = new MultiKeyMap<String, Double>();
		db.getData().forEach((t, m) -> m.getCooccs().forEach((c, v) -> map.put(t, c, v.doubleValue())));
		return map;
	}

	@Override
	public MultiKeyMap<String, Double> getSignificances(final WeightingFunction function) {
		final var k = db.getSentencesCount();
		final var data = db.getData();
		final var map = new MultiKeyMap<String, Double>();
		data.forEach((ti, m) -> {
			final var ki = m.getCount();
			m.getCooccs().forEach((tj, kij) -> {
				final var kj = data.get(tj).getCount();
				final var sig = function.calculate(ki, kj, kij, k);
				map.put(ti, tj, sig);
			});
		});
		return map;
	}

	@Override
	public MultiKeyMap<String, Double> getSignificances(final DirectedWeightingFunction function) {
		final var kmax = db.getMaxSentencesCount();
		final var data = db.getData();
		final var map = new MultiKeyMap<String, Double>();
		data.forEach((ti, m) -> {
			final var ki = m.getCount();
			m.getCooccs().forEach((tj, kij) -> {
				final var kj = data.get(tj).getCount();
				final var dom = ki > kj ? ti : tj;
				final var sub = ki > kj ? tj : ti;
				if (!map.containsKey(sub, dom)) {
					final var sig = function.calculate(kij, kmax);
					map.put(dom, sub, sig);
				}
			});
		});
		return map;
	}

	@Override
	public MultiKeyMap<String, Double> getTermFrequencies() {
		final var map = new MultiKeyMap<String, Double>();
		db.getData().forEach((t, m) -> m.getDocuments().forEach((d, v) -> map.put(t, d, v.doubleValue())));
		return map;
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

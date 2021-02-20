package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.PageRankConfig;
import de.fernuni_hagen.kn.nlp.graph.BreadthFirstGraphSearcher;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calculates the PageRanks for all terms in the DB.
 *
 * @author Nils Wende
 */
public class PageRank {

	private static final int TERM_1 = 0;
	private static final int TERM_2 = 1;

	private final PageRankConfig config;
	private final double weight;
	private final double invWeight;

	public PageRank(final PageRankConfig config) {
		this.config = config;
		weight = config.getWeight();
		invWeight = 1 - weight;
	}

	/**
	 * Calculates the PageRanks for all terms in the DB.
	 *
	 * @param db DB
	 * @return PageRanks
	 */
	public Map<String, Double> calculate(final DBReader db) {
		final var significances = db.getSignificances(config.getWeightingFunction());
		final var collect = getTermMap(significances);
		new BreadthFirstGraphSearcher().findBiggestSubgraph(collect);
		final var map = new MultiKeyMap<String, Double>();
		collect.forEach((t1, l) -> l.forEach(t2 -> map.put(t1, t2, significances.get(t1, t2))));


		final var pageRanks = initPageRanks(collect.keySet());
		for (int i = 0; i < config.getIterations(); i++) {
			calculate(pageRanks, map);
		}
		return normalize(pageRanks);
	}

	private Map<String, List<String>> getTermMap(MultiKeyMap<String, Double> significances) {
		return significances.keySet().stream()
				.collect(Collectors.groupingBy(
						k -> k.getKey(TERM_1),
						Collectors.mapping(k -> k.getKey(TERM_2), Collectors.toList())
				));
	}

	private Map<String, Double> initPageRanks(final Set<String> terms) {
		return terms.stream().collect(Collectors.toMap(t -> t, t -> invWeight));
	}

	private void calculate(final Map<String, Double> pageRanks, final MultiKeyMap<String, Double> significances) {
		significances.forEach((t1, v) -> {
			final double pr = invWeight + weight * sumAdjacentPageRanks(pageRanks, v, significances);
			pageRanks.put(t1, pr);
		});
	}

	private double sumAdjacentPageRanks(final Map<String, Double> pageRanks, final Map<String, Double> v, final MultiKeyMap<String, Double> significances) {
		return v.entrySet().stream()
				.mapToDouble(e -> (pageRanks.get(e.getKey()) * e.getValue()) / significances.get(e.getKey()).size())
				.sum();
	}

	private Map<String, Double> normalize(final Map<String, Double> pageRanks) {
		pageRanks.values().stream()
				.max(Double::compareTo)
				.ifPresent(maxPageRank -> normalize(pageRanks, maxPageRank));
		return pageRanks;
	}

	private void normalize(final Map<String, Double> pageRanks, final double maxPageRank) {
		pageRanks.replaceAll((t, pr) -> pr / maxPageRank);
	}

}

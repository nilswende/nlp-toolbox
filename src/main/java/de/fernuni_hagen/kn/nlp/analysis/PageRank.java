package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.PageRankConfig;
import de.fernuni_hagen.kn.nlp.graph.BreadthFirstGraphSearcher;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calculates the PageRanks for all terms in the DB.
 *
 * @author Nils Wende
 */
public class PageRank {

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
		new BreadthFirstGraphSearcher().findBiggestSubgraph(significances);

		final var pageRanks = initPageRanks(significances.keySet());
		for (int i = 0; i < config.getIterations(); i++) {
			calculate(pageRanks, significances);
		}
		return normalize(pageRanks);
	}

	private Map<String, Double> initPageRanks(final Set<String> terms) {
		return terms.stream().collect(Collectors.toMap(t -> t, t -> invWeight));
	}

	private void calculate(final Map<String, Double> pageRanks, final Map<String, Map<String, Double>> significances) {
		significances.forEach((t1, v) -> {
			final double pr = invWeight + weight * sumAdjacentPageRanks(pageRanks, v, significances);
			pageRanks.put(t1, pr);
		});
	}

	private double sumAdjacentPageRanks(final Map<String, Double> pageRanks, final Map<String, Double> v, final Map<String, Map<String, Double>> significances) {
		return v.keySet().stream()
				.mapToDouble(coocc -> pageRanks.get(coocc) / significances.get(coocc).size())
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

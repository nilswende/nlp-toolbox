package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.analysis.HITS.Scores;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.HITSConfig;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.PageRankConfig;
import de.fernuni_hagen.kn.nlp.db.neo4j.Neo4JReader;

import java.util.Comparator;

import static de.fernuni_hagen.kn.nlp.Logger.logDuration;
import static de.fernuni_hagen.kn.nlp.Logger.logStart;
import static java.util.Comparator.comparingDouble;
import static java.util.Map.Entry.comparingByValue;

/**
 * Analyzes the cooccurrence graph in the database.
 *
 * @author Nils Wende
 */
public class Analysis {

	private final AnalysisConfig config;

	public Analysis(final AnalysisConfig config) {
		this.config = config;
	}

	/**
	 * Analyzes the cooccurrence graph in the database.
	 */
	public void analyze() {
		final var pageRankConfig = config.getPageRankConfig();
		if (pageRankConfig.calculate()) {
			analyzePageRank(pageRankConfig);
		}
		final var hitsConfig = config.getHitsConfig();
		if (hitsConfig.calculate()) {
			analyzeHITS(hitsConfig);
		}
	}

	private void analyzePageRank(final PageRankConfig pageRankConfig) {
		final var start = logStart("PageRank");
		final var pageRanks = new PageRank(pageRankConfig).calculate(new Neo4JReader());
		pageRanks.entrySet().stream()
				.sorted(comparingByValue(Comparator.reverseOrder()))
				.limit(pageRankConfig.getResultLimit())
				.forEach(e -> System.out.println("PageRank of " + e.getKey() + ": " + e.getValue()));
		logDuration("PageRank", start);
	}

	private void analyzeHITS(final HITSConfig hitsConfig) {
		final var start = logStart("HITS");
		final var hits = new HITS(hitsConfig).calculate(new Neo4JReader());
		hits.entrySet().stream()
				.sorted(comparingByValue(comparingDouble(Scores::getAuthorityScore).reversed()))
				.limit(hitsConfig.getResultLimit())
				.forEach(e -> System.out.println("Authority score of " + e.getKey() + ": " + e.getValue().getAuthorityScore()));
		hits.entrySet().stream()
				.sorted(comparingByValue(comparingDouble(Scores::getHubScore).reversed()))
				.limit(hitsConfig.getResultLimit())
				.forEach(e -> System.out.println("Hub score of " + e.getKey() + ": " + e.getValue().getHubScore()));
		logDuration("HITS", start);
	}

}

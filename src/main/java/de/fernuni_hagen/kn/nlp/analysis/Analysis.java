package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.analysis.HITS.Scores;
import de.fernuni_hagen.kn.nlp.config.DocSimConfig;
import de.fernuni_hagen.kn.nlp.config.HITSConfig;
import de.fernuni_hagen.kn.nlp.config.PageRankConfig;

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

	private final DBReader dbReader;

	public Analysis(final DBReader dbReader) {
		this.dbReader = dbReader;
	}

	/**
	 * Analyzes the cooccurrence graph in the database.
	 */
	public void analyze() {
		final PageRankConfig pageRankConfig = null;
		if (pageRankConfig.calculate()) {
			final var start = logStart("PageRank");
			analyzePageRank(pageRankConfig);
			logDuration("PageRank", start);
		}
		final HITSConfig hitsConfig = null;
		if (hitsConfig.calculate()) {
			final var start = logStart("HITS");
			analyzeHITS(hitsConfig);
			logDuration("HITS", start);
		}
		final DocSimConfig docSimConfig = null;
		if (docSimConfig.calculate()) {
			final var start = logStart("DocSim");
			analyzeDocSim(docSimConfig);
			logDuration("DocSim", start);
		}
	}

	private void analyzePageRank(final PageRankConfig pageRankConfig) {
		final var pageRanks = new PageRank(pageRankConfig).calculate(dbReader);
		pageRanks.entrySet().stream()
				.sorted(comparingByValue(Comparator.reverseOrder()))
				.limit(pageRankConfig.getResultLimit())
				.forEach(e -> System.out.println("PageRank of " + e.getKey() + ": " + e.getValue()));
	}

	private void analyzeHITS(final HITSConfig hitsConfig) {
		final var hits = HITS.from(hitsConfig).calculate(dbReader);
		hits.entrySet().stream()
				.sorted(comparingByValue(comparingDouble(Scores::getAuthorityScore).reversed()))
				.limit(hitsConfig.getResultLimit())
				.forEach(e -> System.out.println("Authority score of " + e.getKey() + ": " + e.getValue().getAuthorityScore()));
		hits.entrySet().stream()
				.sorted(comparingByValue(comparingDouble(Scores::getHubScore).reversed()))
				.limit(hitsConfig.getResultLimit())
				.forEach(e -> System.out.println("Hub score of " + e.getKey() + ": " + e.getValue().getHubScore()));
	}

	private void analyzeDocSim(final DocSimConfig docSimConfig) {
		final var similarities = new DocumentSimilarity(docSimConfig).calculate(dbReader);
		if (similarities.isEmpty()) {
			System.out.println("Document similarity: Too few documents");
		} else {
			similarities.forEach((d1, m) -> m.forEach((d2, s) ->
					System.out.println(String.format("Document similarity of '%s' and '%s': %s", d1, d2, s))
			));
		}
	}

}

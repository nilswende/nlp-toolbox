package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.analysis.BooleanRetrieval;
import de.fernuni_hagen.kn.nlp.analysis.CentroidBySpreadingActivation;
import de.fernuni_hagen.kn.nlp.analysis.DocumentSimilarity;
import de.fernuni_hagen.kn.nlp.analysis.HITS;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparingDouble;
import static java.util.Map.Entry.comparingByValue;

/**
 * @author Nils Wende
 */
public class UseCaseExecutor {

	private final List<Class<? extends UseCases>> useCases = List.of(BooleanRetrieval.class, CentroidBySpreadingActivation.class);
	private final DBReader dbReader;
	private final DBWriter dbWriter;

	/*
	JsonUseCaseFactory erzeugt UseCases aus JSON
	config direkt in UseCase-Klassen ohne Zwischenschicht
	Factory auftrennen für App und usecases, evtl aber gemeinsam implementieren
	
	 */

	public UseCaseExecutor(final DBReader dbReader, final DBWriter dbWriter) {
		this.dbReader = dbReader;
		this.dbWriter = dbWriter;
	}

	public void execute(final List<Pair<UseCase, UseCaseConfig>> useCases) {
		for (final Pair<UseCase, UseCaseConfig> useCase : useCases) {
			switch (useCase.getLeft()) {
				case CLEAR_DATABASE:
					dbWriter.deleteAll();
					break;
				case HITS:
			}
		}
	}

	private void execute(final Pair<UseCase, UseCaseConfig> useCase) {

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
				.sorted(comparingByValue(comparingDouble(HITS.Scores::getAuthorityScore).reversed()))
				.limit(hitsConfig.getResultLimit())
				.forEach(e -> System.out.println("Authority score of " + e.getKey() + ": " + e.getValue().getAuthorityScore()));
		hits.entrySet().stream()
				.sorted(comparingByValue(comparingDouble(HITS.Scores::getHubScore).reversed()))
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

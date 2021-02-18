package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.DocSimConfig;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.Map;
import java.util.TreeMap;

/**
 * Calculates the similarity of given documents.
 *
 * @author Nils Wende
 */
public class DocumentSimilarity {

	private final DocSimConfig config;

	public DocumentSimilarity(final DocSimConfig config) {
		this.config = config;
	}

	/**
	 * Calculates the similarity of given documents.
	 *
	 * @param db DB
	 * @return the pairwise document similarities
	 */
	public Map<String, Map<String, Double>> calculate(final DBReader db) {
		final var termFreqs = db.getTermFrequencies();
		final Map<String, Map<String, Double>> normalizedTermFreqs = getNormalizedTermFrequencies(termFreqs);
		final var termWeights = getTermWeights(normalizedTermFreqs);
		final var documentVectors = Maps.invertMapping(termWeights);
		return getSimilarities(documentVectors);
	}

	private Map<String, Map<String, Double>> getNormalizedTermFrequencies(final Map<String, Map<String, Long>> termFreqs) {
		final var sum = termFreqs.values().stream().flatMap(m -> m.values().stream()).mapToDouble(l -> l).sum();
		return Maps.toDoubleMap(termFreqs, l -> l / sum);
	}

	private Map<String, Map<String, Double>> getTermWeights(final Map<String, Map<String, Double>> normalizedTermFreqs) {
		final var docCount = normalizedTermFreqs.values().stream().flatMap(m -> m.keySet().stream()).distinct().count();
		normalizedTermFreqs.forEach((t, m) -> {
			final var idf = Math.log(docCount / (double) m.size());
			m.replaceAll((d, nf) -> nf * idf);
		});
		return normalizedTermFreqs;
	}

	private Map<String, Map<String, Double>> getSimilarities(final Map<String, Map<String, Double>> documentVectors) {
		final var documents = config.getDocuments();
		final var map = new TreeMap<String, Map<String, Double>>();
		for (int i = 0; i < documents.size(); i++) {
			final var d1 = documents.get(i);
			for (int j = i + 1; j < documents.size(); j++) {
				final var d2 = documents.get(j);
				final var sim = config.getSimilarityFunction().calculate(documentVectors.get(d1), documentVectors.get(d2));
				map.computeIfAbsent(d1, k -> new TreeMap<>()).put(d2, sim);
			}
		}
		return map;
	}

}

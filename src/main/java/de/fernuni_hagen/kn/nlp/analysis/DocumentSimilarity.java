package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.DocSimConfig;
import de.fernuni_hagen.kn.nlp.utils.Maps;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Calculates the similarity of given documents.
 *
 * @author Nils Wende
 */
public class DocumentSimilarity {

	private final DocSimConfig config;
	private List<String> documents;

	public DocumentSimilarity(final DocSimConfig config) {
		this.config = config;
		documents = config.getDocuments();
	}

	/**
	 * Calculates the similarity of given documents.
	 *
	 * @param db DB
	 * @return the pairwise document similarities
	 */
	public Map<String, Map<String, Double>> calculate(final DBReader db) {
		final var termFreqs = db.getTermFrequencies();
		replaceDocuments(termFreqs);
		final var normalizedTermFreqs = getNormalizedTermFrequencies(termFreqs);
		final var termWeights = config.useInverseDocFrequency() ? getTermWeights(normalizedTermFreqs) : normalizedTermFreqs;
		final var reducedTermWeights = getReducedTermWeights(termWeights);
		final var documentVectors = Maps.invertMapping(reducedTermWeights);
		return getSimilarities(documentVectors);
	}

	private void replaceDocuments(Map<String, Map<String, Long>> termFreqs) {
		if (CollectionUtils.isEmpty(config.getDocuments())) {
			documents = termFreqs.values().stream().flatMap(m -> m.keySet().stream()).distinct().collect(Collectors.toList());
		}
	}

	private Map<String, Map<String, Double>> getNormalizedTermFrequencies(final Map<String, Map<String, Long>> termFreqs) {
		final var sum = termFreqs.values().stream().flatMap(m -> m.values().stream()).mapToDouble(l -> l).sum();
		return Maps.toDoubleMap(termFreqs, l -> l / sum);
	}

	private Map<String, Map<String, Double>> getTermWeights(final Map<String, Map<String, Double>> normalizedTermFreqs) {
		final var docCount = (double) documents.size();
		normalizedTermFreqs.forEach((t, m) -> {
			final var idf = Math.log(docCount / m.size());
			m.replaceAll((d, nf) -> nf * idf);
		});
		return normalizedTermFreqs;
	}

	private Map<String, Map<String, Double>> getReducedTermWeights(final Map<String, Map<String, Double>> termWeights) {
		final var threshold = config.getWeightThreshold();
		if (threshold > 0) {
			termWeights.forEach((t, m) -> m.values().removeIf(w -> w < threshold));
			termWeights.values().removeIf(Map::isEmpty);
		}
		return termWeights;
	}

	private Map<String, Map<String, Double>> getSimilarities(final Map<String, Map<String, Double>> documentVectors) {
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

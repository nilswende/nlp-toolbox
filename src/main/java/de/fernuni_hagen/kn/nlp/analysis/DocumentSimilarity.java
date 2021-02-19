package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.DocSimConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.util.List;
import java.util.Map;
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
	public MultiKeyMap<String, Double> calculate(final DBReader db) {
		final var termFreqs = db.getTermFrequencies();
		replaceDocuments(termFreqs);
		final var normalizedTermFreqs = getNormalizedTermFrequencies(termFreqs);
		final var termWeights = config.useInverseDocFrequency() ? getTermWeights(normalizedTermFreqs) : normalizedTermFreqs;
		final var reducedTermWeights = getReducedTermWeights(termWeights);
		final var documentVectors = getDocumentVectors(reducedTermWeights);
		return getSimilarities(documentVectors);
	}

	private void replaceDocuments(final MultiKeyMap<String, Double> termFreqs) {
		if (CollectionUtils.isEmpty(config.getDocuments())) {
			documents = termFreqs.keySet().stream().map(k -> k.getKey(1)).distinct().collect(Collectors.toList());
		} else {
			termFreqs.keySet().removeIf(k -> !documents.contains(k.getKey(1)));
		}
	}

	private MultiKeyMap<String, Double> getNormalizedTermFrequencies(final MultiKeyMap<String, Double> termFreqs) {
		final var termSums = termFreqs.entrySet().stream()
				.collect(Collectors.groupingBy(
						e -> e.getKey().getKey(1),
						Collectors.summingDouble(Map.Entry::getValue)));
		termFreqs.replaceAll((k, f) -> f / termSums.get(k.getKey(1)));
		return termFreqs;
	}

	private MultiKeyMap<String, Double> getTermWeights(final MultiKeyMap<String, Double> normalizedTermFreqs) {
		final var docCount = (double) documents.size();
		final var docSums = normalizedTermFreqs.entrySet().stream()
				.collect(Collectors.groupingBy(
						e -> e.getKey().getKey(0),
						Collectors.counting()));
		normalizedTermFreqs.replaceAll((k, nf) -> nf * Math.log10(docCount / docSums.get(k.getKey(0))));
		return normalizedTermFreqs;
	}

	private MultiKeyMap<String, Double> getReducedTermWeights(final MultiKeyMap<String, Double> termWeights) {
		final var threshold = config.getWeightThreshold();
		if (threshold > 0) {
			termWeights.values().removeIf(w -> w < threshold);
		}
		return termWeights;
	}

	private Map<String, Map<String, Double>> getDocumentVectors(final MultiKeyMap<String, Double> reducedTermWeights) {
		return reducedTermWeights.entrySet().stream()
				.collect(Collectors.groupingBy(
						e -> e.getKey().getKey(1),
						Collectors.mapping(e -> e,
								Collectors.toMap(e -> e.getKey().getKey(0), Map.Entry::getValue))));
	}

	private MultiKeyMap<String, Double> getSimilarities(final Map<String, Map<String, Double>> documentVectors) {
		final var similarityFunction = config.getSimilarityFunction();
		final var map = new MultiKeyMap<String, Double>();
		for (int i = 0; i < documents.size(); i++) {
			final var d1 = documents.get(i);
			for (int j = i + 1; j < documents.size(); j++) {
				final var d2 = documents.get(j);
				final var sim = similarityFunction.calculate(documentVectors.get(d1), documentVectors.get(d2));
				map.put(d1, d2, sim);
			}
		}
		return map;
	}

}

package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.Config.AnalysisConfig.DocSimConfig;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static de.fernuni_hagen.kn.nlp.utils.Maps.*;

/**
 * Calculates the similarity of given documents.
 *
 * @author Nils Wende
 */
class DocumentSimilarity {

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
		final var documentVectors = invertMapping(reducedTermWeights);
		return getSimilarities(documentVectors);
	}

	private void replaceDocuments(final Map<String, Map<String, Long>> term2doc) {
		if (CollectionUtils.isEmpty(config.getDocuments())) {
			documents = new ArrayList<>(getInnerKeys(term2doc));
		} else {
			term2doc.forEach((t, docs) -> docs.keySet().removeIf(d -> !documents.contains(d)));
			term2doc.values().removeIf(Map::isEmpty);
		}
	}

	private Map<String, Map<String, Double>> getNormalizedTermFrequencies(final Map<String, Map<String, Long>> term2doc) {
		return invertMapping(
				transformCopy(
						invertMapping(term2doc),
						terms -> terms.values().stream().mapToDouble(d -> d).sum(),
						(sum, l) -> l / sum)
		);
	}

	private Map<String, Map<String, Double>> getTermWeights(final Map<String, Map<String, Double>> term2doc) {
		final var docCount = (double) documents.size();
		return transform(term2doc,
				docs -> Math.log10(docCount / docs.size()),
				(idf, nf) -> nf * idf);
	}

	private Map<String, Map<String, Double>> getReducedTermWeights(final Map<String, Map<String, Double>> term2doc) {
		final var threshold = config.getWeightThreshold();
		if (threshold > 0) {
			term2doc.forEach((t, docs) -> docs.values().removeIf(w -> w < threshold));
			term2doc.values().removeIf(Map::isEmpty);
		}
		return term2doc;
	}

	private Map<String, Map<String, Double>> getSimilarities(final Map<String, Map<String, Double>> doc2term) {
		final var similarityFunction = config.getSimilarityFunction();
		final var map = new TreeMap<String, Map<String, Double>>();
		for (int i = 0; i < documents.size(); i++) {
			final var d1 = documents.get(i);
			for (int j = i + 1; j < documents.size(); j++) {
				final var d2 = documents.get(j);
				final var sim = similarityFunction.calculate(doc2term.get(d1), doc2term.get(d2));
				map.computeIfAbsent(d1, k -> new TreeMap<>()).put(d2, sim);
			}
		}
		return map;
	}

}

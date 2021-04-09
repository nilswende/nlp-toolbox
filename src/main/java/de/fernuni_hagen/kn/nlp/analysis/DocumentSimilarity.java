package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.math.DocSimilarityFunction;
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
public class DocumentSimilarity extends UseCase {

	private boolean useInverseDocFrequency;
	private double weightThreshold;
	private DocSimilarityFunction similarityFunction = DocSimilarityFunction.COSINE;
	private List<String> documents;

	private Result result;

	/**
	 * Document similarity result.
	 */
	public static class Result extends UseCase.Result {
		private final Map<String, Map<String, Double>> similarities;

		Result(final Map<String, Map<String, Double>> similarities) {
			this.similarities = similarities;
		}

		@Override
		protected void printResult() {
			printfMapMap(similarities, "Too few documents", "Document similarity of '%s' and '%s': %s");
		}

		public Map<String, Map<String, Double>> getSimilarities() {
			return similarities;
		}
	}

	@Override
	public void execute(final DBReader dbReader) {
		final var similarities = calculate(dbReader);
		result = new Result(similarities);
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
		final var termWeights = useInverseDocFrequency ? getTermWeights(normalizedTermFreqs) : normalizedTermFreqs;
		final var reducedTermWeights = getReducedTermWeights(termWeights);
		final var documentVectors = invertMapping(reducedTermWeights);
		return getSimilarities(documentVectors);
	}

	private void replaceDocuments(final Map<String, Map<String, Long>> term2doc) {
		if (CollectionUtils.isEmpty(documents)) {
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
		final var threshold = weightThreshold;
		if (threshold > 0) {
			term2doc.forEach((t, docs) -> docs.values().removeIf(w -> w < threshold));
			term2doc.values().removeIf(Map::isEmpty);
		}
		return term2doc;
	}

	private Map<String, Map<String, Double>> getSimilarities(final Map<String, Map<String, Double>> doc2term) {
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

	@Override
	public Result getResult() {
		return result;
	}

	/**
	 * Set the use of the inverse document frequency.
	 *
	 * @param useInverseDocFrequency use the inverse document frequency
	 * @return this object
	 */
	public DocumentSimilarity setUseInverseDocFrequency(final boolean useInverseDocFrequency) {
		this.useInverseDocFrequency = useInverseDocFrequency;
		return this;
	}

	/**
	 * Set the weight threshold, below which a term is not important for its document and ignored.
	 *
	 * @param weightThreshold the weight threshold
	 * @return this object
	 */
	public DocumentSimilarity setWeightThreshold(final double weightThreshold) {
		this.weightThreshold = weightThreshold;
		return this;
	}

	/**
	 * Set the function to calculate the similarity between two documents.
	 *
	 * @param similarityFunction the similarity function
	 * @return this object
	 */
	public DocumentSimilarity setSimilarityFunction(final DocSimilarityFunction similarityFunction) {
		this.similarityFunction = similarityFunction;
		return this;
	}

	/**
	 * Set the list of documents to compare.
	 *
	 * @param documents the list of documents to compare
	 * @return this object
	 */
	public DocumentSimilarity setDocuments(final List<String> documents) {
		this.documents = documents;
		return this;
	}
}

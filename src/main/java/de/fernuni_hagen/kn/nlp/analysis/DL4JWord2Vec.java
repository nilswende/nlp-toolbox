package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.UseCase;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;

import java.util.stream.Collectors;

/**
 * <a href="https://deeplearning4j.konduit.ai/language-processing/word2vec">Word2Vec</a> is a two-layer neural net that processes text.
 *
 * @author Nils Wende
 */
public class DL4JWord2Vec extends UseCase {

	private String document;
	private int minWordFrequency;
	private int layerSize;
	private int seed;
	private int windowSize;

	private transient Result result;

	/**
	 * Word2Vec result.
	 */
	public static class Result extends UseCase.Result {
		Result() {
		}
	}

	/**
	 * This class is needed to be able to use Word2Vec.Builder's fluent interface while maintaining its default values.
	 */
	private static class Builder extends Word2Vec.Builder {
		@Override
		public Word2Vec.Builder minWordFrequency(final int minWordFrequency) {
			return minWordFrequency == 0 ? this : super.minWordFrequency(minWordFrequency);
		}

		@Override
		public Word2Vec.Builder layerSize(final int layerSize) {
			return layerSize == 0 ? this : super.layerSize(layerSize);
		}

		@Override
		public Word2Vec.Builder seed(final long randomSeed) {
			return seed == 0 ? this : super.seed(randomSeed);
		}

		@Override
		public Word2Vec.Builder windowSize(final int windowSize) {
			return window == 0 ? this : super.windowSize(windowSize);
		}
	}

	@Override
	protected void execute(final DBReader dbReader) {
		final var sentences = dbReader.getAllSentencesInDocument(document).stream()
				.map(l -> String.join(StringUtils.SPACE, l))
				.collect(Collectors.toList());
		final var sentenceIterator = new CollectionSentenceIterator(sentences);
		final var tokenizerFactory = new DefaultTokenizerFactory();
		final var vec = new Builder()
				.minWordFrequency(minWordFrequency)
				.layerSize(layerSize)
				.seed(seed)
				.windowSize(windowSize)
				.iterate(sentenceIterator)
				.tokenizerFactory(tokenizerFactory)
				.build();
		vec.fit();
		result = new Result();
	}

	@Override
	public Result getResult() {
		return result;
	}

	/**
	 * Set the document to process.
	 *
	 * @param document the document to process
	 * @return this object
	 */
	public DL4JWord2Vec setDocument(final String document) {
		this.document = document;
		return this;
	}

	/**
	 * Set the Word2Vec minWordFrequency.
	 *
	 * @param minWordFrequency the Word2Vec minWordFrequency
	 * @return this object
	 */
	public DL4JWord2Vec setMinWordFrequency(final int minWordFrequency) {
		this.minWordFrequency = minWordFrequency;
		return this;
	}

	/**
	 * Set the Word2Vec layerSize.
	 *
	 * @param layerSize the Word2Vec layerSize
	 * @return this object
	 */
	public DL4JWord2Vec setLayerSize(final int layerSize) {
		this.layerSize = layerSize;
		return this;
	}

	/**
	 * Set the Word2Vec seed.
	 *
	 * @param seed the Word2Vec seed
	 * @return this object
	 */
	public DL4JWord2Vec setSeed(final int seed) {
		this.seed = seed;
		return this;
	}

	/**
	 * Set the Word2Vec windowSize.
	 *
	 * @param windowSize the Word2Vec windowSize
	 * @return this object
	 */
	public DL4JWord2Vec setWindowSize(final int windowSize) {
		this.windowSize = windowSize;
		return this;
	}
}

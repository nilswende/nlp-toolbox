package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.config.UseCaseConfig;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;

import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * <a href="https://deeplearning4j.konduit.ai/language-processing/word2vec">Word2Vec</a> is a two-layer neural net that processes text.
 *
 * @author Nils Wende
 */
public class DL4JWord2Vec extends UseCase {

	private final Config config;

	public DL4JWord2Vec(final Config config) {
		this.config = config;
	}

	/**
	 * DL4JWord2Vec config.
	 */
	public static class Config extends UseCaseConfig {
		private String document;
		private int minWordFrequency;
		private int layerSize;
		private int seed;
		private int windowSize;

		public String getDocument() {
			return document;
		}

		public int getMinWordFrequency() {
			return minWordFrequency;
		}

		public int getLayerSize() {
			return layerSize;
		}

		public int getSeed() {
			return seed;
		}

		public int getWindowSize() {
			return windowSize;
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
		final var sentences = dbReader.getAllSentencesInDocument(Path.of(config.getDocument())).stream()
				.map(l -> String.join(StringUtils.SPACE, l))
				.collect(Collectors.toList());
		final var sentenceIterator = new CollectionSentenceIterator(sentences);
		final var tokenizerFactory = new DefaultTokenizerFactory();
		final var vec = new Builder()
				.minWordFrequency(config.getMinWordFrequency())
				.layerSize(config.getLayerSize())
				.seed(config.getSeed())
				.windowSize(config.getWindowSize())
				.iterate(sentenceIterator)
				.tokenizerFactory(tokenizerFactory)
				.build();
		vec.fit();
	}
}

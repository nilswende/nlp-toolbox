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

		public String getDocument() {
			return document;
		}
	}

	@Override
	protected void execute(final DBReader dbReader) {
		final var sentences = dbReader.getAllSentencesInDocument(Path.of(config.getDocument())).stream()
				.map(l -> String.join(StringUtils.SPACE, l))
				.collect(Collectors.toList());
		final var sentenceIterator = new CollectionSentenceIterator(sentences);
		final var tokenizerFactory = new DefaultTokenizerFactory();
		final var vec = new Word2Vec.Builder()
				.minWordFrequency(5)
				.layerSize(100)
				.seed(42)
				.windowSize(5)
				.iterate(sentenceIterator)
				.tokenizerFactory(tokenizerFactory)
				.build();
		vec.fit();
	}
}

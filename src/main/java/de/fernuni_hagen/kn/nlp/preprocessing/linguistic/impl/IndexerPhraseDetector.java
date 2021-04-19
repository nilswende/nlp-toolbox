package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.PhraseDetector;
import org.apache.commons.lang3.StringUtils;
import te.indexer.Indexer;
import te.indexer.Word;

import java.util.List;
import java.util.stream.Collectors;

import static de.fernuni_hagen.kn.nlp.preprocessing.linguistic.PreprocessingUtils.cast;

/**
 * Detects phrases in a text using the ASV Indexer class.
 *
 * @author Nils Wende
 */
public class IndexerPhraseDetector implements PhraseDetector {

	private final int asvLanguage;

	/**
	 * Creates a new instance.
	 *
	 * @param asvLanguage the language constant as defined by the ASV library
	 */
	public IndexerPhraseDetector(final int asvLanguage) {
		this.asvLanguage = asvLanguage;
	}

	@Override
	public List<String> detectPhrases(final List<String> sentences) {
		final var indexer = createIndexer();
		final var text = String.join(StringUtils.SPACE, sentences);
		indexer.prepare(text);
		return getPhrases(indexer);
	}

	private Indexer createIndexer() {
		final var indexer = new Indexer();
		indexer.setLanguage(asvLanguage);
		indexer.getParameters().setStemming(false);
		return indexer;
	}

	private List<String> getPhrases(final Indexer indexer) {
		final List<Word> phrases = cast(indexer.getPhrases());
		return phrases.stream()
				.map(Word::getWordStr)
				.collect(Collectors.toList());
	}

}

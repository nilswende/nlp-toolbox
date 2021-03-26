package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.PhraseExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import te.indexer.Indexer;
import te.indexer.Word;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.fernuni_hagen.kn.nlp.preprocessing.linguistic.PreprocessingUtils.cast;

/**
 * Extracts phrases from a text using the ASV Indexer class.
 *
 * @author Nils Wende
 */
public class IndexerPhraseExtractor implements PhraseExtractor {

	private final int asvLanguage;

	public IndexerPhraseExtractor(final int asvLanguage) {
		this.asvLanguage = asvLanguage;
	}

	@Override
	public Pair<List<String>, List<String>> extractPhrases(final Stream<String> sentences) {
		final var sentenceList = sentences.collect(Collectors.toList());
		final Indexer indexer = createIndexer();
		final var text = String.join(StringUtils.SPACE, sentenceList);
		indexer.prepare(text);
		final List<String> phrases = getPhrases(indexer);
		return Pair.of(sentenceList, phrases);
	}

	private Indexer createIndexer() {
		final Indexer indexer = new Indexer();
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

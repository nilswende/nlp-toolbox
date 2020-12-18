package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.PhraseExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import te.indexer.Indexer;
import te.indexer.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.fernuni_hagen.kn.nlp.preprocessing.Utils.cast;

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
	public List<Pair<String, List<String>>> extractPhrases(final List<String> sentences) {
		final Indexer indexer = createIndexer();
		final var text = String.join(StringUtils.SPACE, sentences);
		indexer.prepare(text);
		final List<String> phrases = getPhrases(indexer);
		return getPairs(sentences, phrases);
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
				.map(p -> p.getPos().endsWith("A N") //TODO why?
						? StringUtils.uncapitalize(p.getWordStr())
						: p.getWordStr())
				.collect(Collectors.toList());
	}

	private List<Pair<String, List<String>>> getPairs(final List<String> sentences, final List<String> phrases) {
		return sentences.stream().map(s -> getPair(s, phrases)).collect(Collectors.toList());
	}

	private Pair<String, List<String>> getPair(final String sentence, final List<String> phrases) {
		String extractedSentence = sentence;
		final var extractedPhrases = new ArrayList<String>();
		for (final String phrase : phrases) {
			if (sentence.contains(phrase)) {
				extractedSentence = StringUtils.remove(sentence, phrase);
				extractedPhrases.add(phrase);
			}
		}
		return Pair.of(extractedSentence, extractedPhrases);
	}

}

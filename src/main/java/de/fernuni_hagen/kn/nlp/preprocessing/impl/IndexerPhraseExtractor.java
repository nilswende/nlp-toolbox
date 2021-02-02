package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.PhraseExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import te.indexer.Indexer;
import te.indexer.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	public Stream<Pair<String, List<String>>> extractPhrases(final Stream<String> sentences) {
		final var sentenceList = sentences.collect(Collectors.toList());
		final Indexer indexer = createIndexer();
		final var text = String.join(StringUtils.SPACE, sentenceList);
		indexer.prepare(text);
		final List<String> phrases = getPhrases(indexer);
		return getPairs(sentenceList, phrases);
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

	private Stream<Pair<String, List<String>>> getPairs(final List<String> sentences, final List<String> phrases) {
		return sentences.stream().map(s -> getPair(s, phrases));
	}

	private Pair<String, List<String>> getPair(final String sentence, final List<String> phrases) {
		String extractedSentence = sentence;
		final var extractedPhrases = new ArrayList<String>();
		for (final String phrase : phrases) {
			if (extractedSentence.contains(phrase)) {
				extractedSentence = StringUtils.remove(extractedSentence, phrase);
				extractedPhrases.add(phrase);
			}
		}
		return Pair.of(extractedSentence, extractedPhrases);
	}

}

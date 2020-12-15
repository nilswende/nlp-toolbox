package de.fernuni_hagen.kn.nlp.workflow;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import te.indexer.Indexer;
import te.indexer.Word;
import te.utils.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static de.fernuni_hagen.kn.nlp.workflow.Utils.cast;

/**
 * Extracts phrases from sentences using the ASV Indexer class.
 *
 * @author Nils Wende
 */
public class IndexerPhraseExtractor implements PhraseExtractor {

	private final Indexer indexer;

	IndexerPhraseExtractor(final Locale locale) {
		indexer = new Indexer();
		indexer.setLanguage(mapLanguage(locale));
		indexer.getParameters().setStemming(false);
	}

	private int mapLanguage(final Locale locale) {
		switch (locale.getLanguage()) {
			case "de":
				return Parameters.DE;
			case "en":
				return Parameters.EN;
			default:
				throw new IllegalArgumentException("Unsupported locale: " + locale);
		}
	}

	@Override
	public List<Pair<String, List<String>>> extractPhrases(final List<String> sentences) {
		final var text = String.join(" ", sentences);
		indexer.prepare(text);
		final List<String> phrases = getPhrases();
		return getPairs(sentences, phrases);
	}

	private List<String> getPhrases() {
		final var strings = new ArrayList<String>();
		final List<Word> phrases = cast(indexer.getPhrases());
		for (final Word phrase : phrases) {
			var wordStr = phrase.getWordStr();
			if (phrase.getPos().endsWith("A N")) {
				wordStr = StringUtils.uncapitalize(wordStr);
			}
			strings.add(wordStr);
		}
		return strings;
	}

	private List<Pair<String, List<String>>> getPairs(final List<String> sentences, final List<String> phrases) {
		final var pairs = new ArrayList<Pair<String, List<String>>>(sentences.size());
		for (final String sentence : sentences) {
			String extractedSentence = sentence;
			final var extractedPhrases = new ArrayList<String>();
			for (final String phrase : phrases) {
				if (sentence.contains(phrase)) {
					extractedSentence = StringUtils.remove(sentence, phrase);
					extractedPhrases.add(phrase);
				}
			}
			pairs.add(Pair.of(extractedSentence, extractedPhrases));
		}
		return pairs;
	}

}

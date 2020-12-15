package de.fernuni_hagen.kn.nlp.workflow;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import te.indexer.Indexer;
import te.indexer.Word;
import te.utils.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
		final List<Word> phrases = cast(indexer.getPhrases());
		return phrases.stream()
				.map(p -> p.getPos().endsWith("A N") ? StringUtils.uncapitalize(p.getWordStr()) : p.getWordStr())
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

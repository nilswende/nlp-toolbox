package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.StopWordFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.TaggedWord;
import de.fernuni_hagen.kn.nlp.preprocessing.Utils;
import te.utils.ExternalData;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.fernuni_hagen.kn.nlp.preprocessing.Utils.cast;

/**
 * Removes stop words from a sentence using the ASV library.
 *
 * @author Nils Wende
 */
public class ASVStopWordFilter implements StopWordFilter {

	private final Locale locale;
	private final Set<String> stopWords;

	/**
	 * Constructor.
	 *
	 * @param locale the sentence's language
	 */
	public ASVStopWordFilter(final Locale locale) {
		this.locale = locale;
		final ExternalData ed = ExternalData.getInstance(Utils.mapLanguage(locale));
		final Set<String> set = cast(ed.getStopWordMap());
		stopWords = set.stream()
				.map(this::normalize)
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Stream<TaggedWord> apply(final Stream<TaggedWord> sentence) {
		return sentence.filter(w -> !stopWords.contains(normalize(w.getTerm())));
	}

	private String normalize(final String word) {
		return word.toLowerCase(locale);
	}

}

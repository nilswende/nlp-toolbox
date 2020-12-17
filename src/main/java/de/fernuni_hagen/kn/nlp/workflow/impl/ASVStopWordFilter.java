package de.fernuni_hagen.kn.nlp.workflow.impl;

import de.fernuni_hagen.kn.nlp.workflow.StopWordFilter;
import de.fernuni_hagen.kn.nlp.workflow.Utils;
import te.utils.ExternalData;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.fernuni_hagen.kn.nlp.workflow.Utils.cast;

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
				.map(s -> s.toLowerCase(locale))
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Stream<String> filter(final Stream<String> sentence) {
		return sentence.filter(w -> !stopWords.contains(normalize(w)));
	}

	private String normalize(final String word) {
		return word.substring(0, word.lastIndexOf('|')).toLowerCase(locale);
	}

}

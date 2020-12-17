package de.fernuni_hagen.kn.nlp.workflow.impl;

import de.fernuni_hagen.kn.nlp.workflow.StopWordFilter;
import de.fernuni_hagen.kn.nlp.workflow.Utils;
import org.apache.commons.lang3.StringUtils;
import te.utils.ExternalData;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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
	public String filter(final String sentence) {
		return Arrays.stream(sentence.split(StringUtils.SPACE))
				.filter(w -> !stopWords.contains(normalize(w)))
				.collect(Collectors.joining(StringUtils.SPACE));
	}

	private String normalize(final String word) {
		return word.substring(0, word.lastIndexOf('|')).toLowerCase(locale);
	}

}

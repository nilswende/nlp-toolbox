package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.SentenceCleaner;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Removes unwanted characters from a sentence using regular expressions.
 *
 * @author Nils Wende
 */
public class RegexSentenceCleaner implements SentenceCleaner {

	private static final Pattern DISALLOWED = Pattern.compile("[^\\p{Alnum}äöüÄÖÜß|\\-# ]");

	@Override
	public String clean(final CharSequence sentence) {
		return DISALLOWED.matcher(sentence).replaceAll(StringUtils.EMPTY);
	}

}

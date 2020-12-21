package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.CharacterRemover;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Removes unwanted characters from a CharSequence using regular expressions.
 *
 * @author Nils Wende
 */
public class RegexCharacterRemover implements CharacterRemover {

	private static final Pattern WHITESPACE = Pattern.compile("[\\v\\h]+");
	private static final Pattern ALLOWED = Pattern.compile("[^\\p{Alnum}äöüÄÖÜß|\\-# ]");

	@Override
	public String removeCharacters(final CharSequence chars) {
		final var singleSpaced = WHITESPACE.matcher(chars).replaceAll(StringUtils.SPACE);
		final var cleaned = ALLOWED.matcher(singleSpaced).replaceAll(StringUtils.EMPTY);
		return cleaned.stripTrailing();
	}

}

package de.fernuni_hagen.kn.nlp.input.impl;

import de.fernuni_hagen.kn.nlp.input.WhitespaceRemover;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Removes excess whitespace from a CharSequence using a regular expression.
 *
 * @author Nils Wende
 */
public class RegexWhitespaceRemover implements WhitespaceRemover {

	private static final Pattern WHITESPACE = Pattern.compile("[\\v\\h]+");

	@Override
	public String removeWhitespace(final CharSequence chars) {
		return WHITESPACE.matcher(chars).replaceAll(StringUtils.SPACE).stripTrailing();
	}

}

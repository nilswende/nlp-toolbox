package de.fernuni_hagen.kn.nlp.text;

import de.fernuni_hagen.kn.nlp.input.WhitespaceRemover;

import java.util.regex.Pattern;

/**
 * Removes excess whitespace from a CharSequence using a regular expression.
 *
 * @author Nils Wende
 */
public class RegexWhitespaceRemover implements WhitespaceRemover {

	private static final Pattern WHITESPACE = Pattern.compile("[\\v\\h]+");
	private static final String SPACE = " ";

	@Override
	public String removeWhitespace(final CharSequence chars) {
		return WHITESPACE.matcher(chars).replaceAll(SPACE).stripTrailing();
	}

}

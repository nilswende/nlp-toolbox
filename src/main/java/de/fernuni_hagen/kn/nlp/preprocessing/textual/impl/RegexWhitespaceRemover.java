package de.fernuni_hagen.kn.nlp.preprocessing.textual.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.textual.WhitespaceRemover;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Removes excess whitespace from a CharSequence using a regular expression.
 *
 * @author Nils Wende
 */
public class RegexWhitespaceRemover implements WhitespaceRemover {

	private static final Pattern H_WHITESPACE = Pattern.compile("\\h+");
	private static final Pattern LINEBREAK = Pattern.compile("\\R");

	@Override
	public String removeWhitespace(final CharSequence chars) {
		var tmp = chars;
		tmp = H_WHITESPACE.matcher(tmp).replaceAll(StringUtils.SPACE);
		tmp = reduceWhitespaces(tmp);
		return tmp.toString();
	}

	private CharSequence reduceWhitespaces(final CharSequence chars) {
		final var split = LINEBREAK.split(chars);
		return split.length == 1 ? split[0].strip() : reduceWhitespaces(chars, split);
	}

	private CharSequence reduceWhitespaces(final CharSequence chars, final String[] split) {
		final var sb = new StringBuilder(chars.length());
		for (int i = 0; i < split.length; i++) {
			final String s = split[i];
			if (!s.isBlank()) {
				sb.append(s.strip());
				if (i < split.length - 1) {
					final var next = split[i + 1];
					if (next.isEmpty() || Character.isWhitespace(next.charAt(0))) {
						sb.append(StringUtils.LF);
						while (i < split.length - 1 && split[i + 1].isBlank()) {
							i++;
						}
					} else {
						sb.append(StringUtils.SPACE);
					}
				}
			}
		}
		return sb;
	}

}

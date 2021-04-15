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
		final var lines = LINEBREAK.split(chars);
		return lines.length == 1 ? lines[0].strip() : reduceWhitespaces(chars, lines);
	}

	private CharSequence reduceWhitespaces(final CharSequence chars, final String[] lines) {
		final var sb = new StringBuilder(chars.length());
		for (int i = 0, length = lines.length; i < length; i++) {
			final String line = lines[i];
			if (!line.isBlank()) {
				final var stripped = line.strip();
				sb.append(stripped);
				if (i < length - 1) {
					final var next = lines[i + 1];
					final var whitespaceNext = next.isEmpty() || Character.isWhitespace(next.charAt(0));
					if (stripped.endsWith("-")) {
						if (!whitespaceNext && Character.isLowerCase(next.charAt(0))) {
							sb.setLength(sb.length() - 1);
						}
					} else if (whitespaceNext) {
						sb.append(StringUtils.LF);
					} else {
						sb.append(StringUtils.SPACE);
					}
					while (i < length - 1 && lines[i + 1].isBlank()) {
						i++;
					}
				}
			}
		}
		return sb;
	}

}

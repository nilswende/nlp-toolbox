package de.fernuni_hagen.kn.nlp.preprocessing.textual.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.textual.WhitespaceRemover;

import java.util.regex.Pattern;

/**
 * Removes excess whitespace from a CharSequence using a regular expression.<br>
 * Tries to emulate the whitespace removal of {@code de.texttech.cc.Text2Satz} as closely as possible.
 *
 * @author Nils Wende
 */
public class RegexWhitespaceRemover implements WhitespaceRemover {

	private static final Pattern LINEBREAKS = Pattern.compile("\\R{2}");
	private static final char LF = '\n';
	private static final char SPACE = ' ';

	@Override
	public String removeWhitespace(final CharSequence chars) {
		return reduceWhitespaces(chars).toString();
	}

	private CharSequence reduceWhitespaces(final CharSequence chars) {
		StringBuilder sb = null;
		int start = 0;
		for (int i = 0; i < chars.length(); i++) {
			final var c = chars.charAt(i);
			if (c == SPACE || c == LF || Character.isWhitespace(c)) {
				final int wsEnd = getWhitespaceEnd(chars, i);
				final var wsLength = wsEnd - i;
				if (wsLength != 1 || c != SPACE) {
					if (sb == null) {
						sb = new StringBuilder(chars.length());
						sb.append(chars, start, i);
						start = i;
					}
					if (i != 0 && chars.charAt(i - 1) == '-') {
						if (wsLength == 1 && i + wsLength < chars.length() && Character.isLowerCase(chars.charAt(i + wsLength))) {
							sb.setLength(sb.length() - 1);
						}
					} else if (i == 0) {
						sb.append(LF);
					} else {
						sb.append(chars, start, i);
						if (wsLength == 1) {
							sb.append(SPACE);
						} else if (wsLength == 2) {
							if (LINEBREAKS.matcher(chars).region(i, wsEnd).matches()) {
								sb.append(LF);
							} else {
								sb.append(SPACE);
							}
						} else {
							sb.append(LF);
						}
					}
					i += wsLength;
					start = wsEnd;
				}
			}
		}
		if (sb == null) {
			sb = new StringBuilder(chars);
		} else {
			sb.append(chars, start, chars.length());
		}
		stripTrailing(sb);
		return sb;
	}

	private int getWhitespaceEnd(final CharSequence chars, final int i) {
		int end = i + 1;
		while (end < chars.length() && Character.isWhitespace(chars.charAt(end))) {
			end++;
		}
		return end;
	}

	private void stripTrailing(final StringBuilder chars) {
		int end = chars.length() - 1;
		while (end >= 0 && Character.isWhitespace(chars.charAt(end))) {
			end--;
		}
		chars.setLength(end + 1);
	}

}

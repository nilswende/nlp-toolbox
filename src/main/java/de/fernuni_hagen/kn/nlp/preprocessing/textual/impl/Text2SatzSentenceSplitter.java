package de.fernuni_hagen.kn.nlp.preprocessing.textual.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.textual.SentenceSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Splits a CharSequence into sentences.<br>
 * Tries to emulate the splitting done by {@code de.texttech.cc.Text2Satz} as closely as possible.
 *
 * @author Nils Wende
 */
public class Text2SatzSentenceSplitter implements SentenceSplitter {

	private static final Pattern HORIZONTAL_WS = Pattern.compile("\\h");
	private static final Pattern SINGLE_LINEBREAK = Pattern.compile("\\h*\\R?\\h*");
	private static final char SPACE = ' ';

	@Override
	public List<String> split(final CharSequence chars) {
		final var sentences = new ArrayList<String>();
		final int end = chars.length();
		final var sb = new StringBuilder(end);
		final var horizontalWs = HORIZONTAL_WS.matcher(chars);
		final var linebreak = SINGLE_LINEBREAK.matcher(chars);
		int start = 0;
		for (int i = 0; i < end; i++) {
			final var c = chars.charAt(i);
			if (Character.isWhitespace(c)) {
				final int wsEnd = getWhitespaceEnd(chars, i);
				final int wsLength = wsEnd - i;
				if (c != SPACE || wsLength > 1) {
					sb.append(chars, start, i);
					if (i > 0) {
						if (chars.charAt(i - 1) == '-') {
							if (wsLength == 1 && wsEnd < end && Character.isLowerCase(chars.charAt(wsEnd))) {
								sb.setLength(sb.length() - 1);
							} else if (horizontalWs.region(i, i + 1).matches()) {
								sentences.add(sb.toString());
								sb.setLength(0);
							}
						} else {
							if (wsEnd < end && (wsLength == 1 || linebreak.region(i, wsEnd).matches())) {
								sb.append(SPACE);
							} else {
								sentences.add(sb.toString());
								sb.setLength(0);
							}
						}
					}
					i += wsLength;
					start = wsEnd;
				}
			}
		}
		if (start < end) {
			sb.append(chars, start, end);
			sentences.add(sb.toString().stripTrailing());
		}
		return sentences;
	}

	private int getWhitespaceEnd(final CharSequence chars, final int i) {
		int end = i + 1;
		while (end < chars.length() && Character.isWhitespace(chars.charAt(end))) {
			end++;
		}
		return end;
	}

}

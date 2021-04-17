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

	private static final Pattern LINEBREAKS = Pattern.compile("\\R{2}");
	private static final char SPACE = ' ';

	@Override
	public List<String> split(final CharSequence chars) {
		final var sentences = new ArrayList<String>();
		StringBuilder sb = null;
		int start = 0;
		for (int i = 0; i < chars.length(); i++) {
			final var c = chars.charAt(i);
			if (Character.isWhitespace(c)) {
				final int wsEnd = getWhitespaceEnd(chars, i);
				final var wsLength = wsEnd - i;
				if (wsLength != 1 || c != SPACE) {
					if (sb == null) {
						sb = new StringBuilder(chars.length());
						sb.append(chars, 0, i);
						start = i;
					}
					if (i != 0) {
						if (chars.charAt(i - 1) == '-') {
							if (wsLength == 1 && i + wsLength < chars.length() && Character.isLowerCase(chars.charAt(i + wsLength))) {
								sb.setLength(sb.length() - 1);
							} else {
								sb.append(chars, start, i);
							}
						} else {
							sb.append(chars, start, i);
							if (wsLength == 1 || (wsLength == 2 && !LINEBREAKS.matcher(chars).region(i, wsEnd).matches())) {
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
		if (sb == null) {
			return List.of(chars.toString().stripTrailing());
		}
		if (start < chars.length()) {
			sb.append(chars, start, chars.length());
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

package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.Iterator;
import java.util.List;

/**
 * Iterates over the phrases within a sentence.
 *
 * @author Nils Wende
 */
public class PhraseIterator implements Iterator<String> {

	private final String sentence;
	private final List<String> phrases;
	private int pos = -1;

	/**
	 * Constructs a new PhraseIterator.
	 *
	 * @param sentence the sentence to iterate over
	 * @param phrases  the phrases within the sentence
	 */
	public PhraseIterator(final String sentence, final List<String> phrases) {
		this.sentence = sentence;
		this.phrases = phrases;
	}

	/**
	 * Returns true if the sentence has more phrases.
	 *
	 * @return true, if the sentence has more phrases
	 */
	@Override
	public boolean hasNext() {
		return phrases.stream().anyMatch(p -> sentence.indexOf(p, pos + 1) != -1);
	}

	/**
	 * Returns the next phrase in the sentence.
	 *
	 * @return the next phrase in the sentence
	 */
	@Override
	public String next() {
		String phrase = null;
		int index = Integer.MAX_VALUE;
		for (final String p : phrases) {
			final var i = sentence.indexOf(p, pos + 1);
			if (i != -1 && i < index) {
				phrase = p;
				index = i;
			}
		}
		pos = index;
		return phrase;
	}

	/**
	 * Returns the position of the current phrase in the sentence.
	 *
	 * @return the position of the current phrase in the sentence
	 */
	public int position() {
		return pos;
	}

}

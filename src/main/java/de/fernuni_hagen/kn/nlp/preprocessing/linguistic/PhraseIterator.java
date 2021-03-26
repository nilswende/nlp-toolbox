package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterates over the phrases within a sentence.
 *
 * @author Nils Wende
 */
public class PhraseIterator implements Iterator<String> {

	private final String sentence;
	private final List<String> phrases;

	private String nextPhrase;
	private int nextPos;
	private int pos = -1;

	private StringBuilder builder;

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
		String phrase = null;
		int index = Integer.MAX_VALUE;
		for (final String p : phrases) {
			final var i = nextPosition(p);
			if (i != -1 && i < index) {
				phrase = p;
				index = i;
			}
		}
		nextPhrase = phrase;
		nextPos = index;
		return index != Integer.MAX_VALUE;
	}

	private int nextPosition(final String phrase) {
		return (builder == null
				? sentence.indexOf(phrase, pos + 1)
				: builder.indexOf(phrase, pos + 1));
	}

	/**
	 * Returns the next phrase in the sentence.
	 *
	 * @return the next phrase in the sentence
	 */
	@Override
	public String next() {
		if (nextPhrase == null) {
			throw new NoSuchElementException();
		}
		pos = nextPos;
		return nextPhrase;
	}

	/**
	 * Removes the current phrase from the underlying sentence.
	 */
	@Override
	public void remove() {
		if (builder == null) {
			builder = new StringBuilder(sentence);
		}
		builder.delete(pos, pos + nextPhrase.length());
	}

	/**
	 * Removes all phrases from the underlying sentence.
	 *
	 * @return the underlying sentence
	 */
	public String removeAll() {
		forEachRemaining(p -> remove());
		return getSentence();
	}

	/**
	 * Returns the position of the current phrase in the sentence.
	 *
	 * @return the position of the current phrase in the sentence
	 */
	public int position() {
		return pos;
	}

	/**
	 * Returns the underlying sentence.
	 *
	 * @return the underlying sentence
	 */
	public String getSentence() {
		return builder == null ? sentence : builder.toString();
	}

}

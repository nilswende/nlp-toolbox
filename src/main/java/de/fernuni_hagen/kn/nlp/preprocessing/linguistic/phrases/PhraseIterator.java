package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.phrases;

import java.util.ArrayList;
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
	private int nextPos = -1;
	private int pos = -1;

	private StringBuilder builder;
	private boolean removed = true;

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
		findNext();
		return nextPhrase != null;
	}

	private void findNext() {
		String phrase = null;
		int index = Integer.MAX_VALUE;
		for (final String p : phrases) {
			final int i = nextPosition(p);
			if (i != -1 && i < index) {
				phrase = p;
				index = i;
			}
		}
		nextPhrase = phrase;
		nextPos = index;
	}

	private int nextPosition(final String phrase) {
		return builder == null
				? sentence.indexOf(phrase, pos + 1)
				: builder.indexOf(phrase, pos + 1);
	}

	/**
	 * Returns the next phrase in the sentence.
	 *
	 * @return the next phrase in the sentence
	 */
	@Override
	public String next() {
		checkNext();
		removed = false;
		pos = nextPos;
		return nextPhrase;
	}

	private void checkNext() {
		if (pos == nextPos) {
			findNext();
		}
		if (nextPhrase == null) {
			throw new NoSuchElementException();
		}
	}

	/**
	 * Removes the current phrase from the underlying sentence.
	 */
	@Override
	public void remove() {
		if (removed) {
			throw new IllegalStateException();
		}
		if (builder == null) {
			builder = new StringBuilder(sentence);
		}
		removePhrase();
		removed = true;
	}

	private void removePhrase() {
		int start = pos;
		int end = start + nextPhrase.length();
		final var isStart = start == 0;
		final var isEnd = end == builder.length();
		final var trimStart = !isStart && isWhitespace(start - 1);
		final var trimEnd = !isEnd && isWhitespace(end);
		if (trimStart && isEnd || trimStart && trimEnd) {
			start--;
		} else if (trimEnd && isStart) {
			end++;
		}
		builder.delete(start, end);
	}

	private boolean isWhitespace(final int index) {
		return Character.isWhitespace(builder.charAt(index));
	}

	/**
	 * Removes all phrases from the underlying sentence.
	 *
	 * @return the removed phrases
	 */
	public List<String> removeAll() {
		final var removedPhrases = new ArrayList<String>();
		while (hasNext()) {
			final var phrase = next();
			removedPhrases.add(phrase);
			remove();
		}
		return removedPhrases;
	}

	/**
	 * Returns the index of the current phrase in the sentence.
	 *
	 * @return the index of the current phrase in the sentence
	 */
	public int getIndex() {
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

package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.stream.Stream;

/**
 * A POS tagged word.
 *
 * @author Nils Wende
 */
public class TaggedWord {

	private static final boolean DEBUG = false;

	private final String term;
	private final String tag;
	private final Tagset tagset;

	public TaggedWord(final String term, final TaggedWord taggedWord) {
		this.term = term;
		this.tag = taggedWord.getTag();
		this.tagset = taggedWord.getTagset();
	}

	public TaggedWord(final String term, final String tag, final Tagset tagset) {
		this.term = term;
		this.tag = tag;
		this.tagset = tagset;
	}

	/**
	 * Creates TaggedWords from a tagged sentence.
	 *
	 * @param sentence a tagged sentence
	 * @param tagset   the tagset used for tagging
	 * @return TaggedWords
	 */
	public static Stream<TaggedWord> from(final Stream<String> sentence, final Tagset tagset) {
		if (DEBUG) {
			System.out.println();
		}
		return sentence.map(w -> TaggedWord.from(w, tagset));
	}

	/**
	 * Creates a TaggedWord from a tagged word.
	 *
	 * @param taggedWord a tagged word
	 * @param tagset     the tagset used for tagging
	 * @return TaggedWord
	 */
	public static TaggedWord from(final String taggedWord, final Tagset tagset) {
		final var separator = tagset.getTagSeparator();
		final var pos = taggedWord.lastIndexOf(separator);
		final var word = new TaggedWord(taggedWord.substring(0, pos), taggedWord.substring(pos + separator.length()), tagset);
		if (DEBUG) {
			System.out.print(word + " ");
		}
		return word;
	}

	/**
	 * Checks if this word is a noun.
	 *
	 * @return true, if this word is tagged as a noun.
	 */
	public boolean isNoun() {
		return tag.startsWith(tagset.getNounTag());
	}

	@Override
	public String toString() {
		return term + tagset.getTagSeparator() + tag;
	}

	public String getTerm() {
		return term;
	}

	public String getTag() {
		return tag;
	}

	public Tagset getTagset() {
		return tagset;
	}

}

package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.stream.Stream;

/**
 * A POS tagged word.
 *
 * @author Nils Wende
 */
public class TaggedWord {

	private final String term;
	private final String tag;
	private final Tagset tagset;

	public TaggedWord(final String term, final TaggedWord taggedWord) {
		this.term = term;
		this.tag = taggedWord.getTag();
		this.tagset = taggedWord.getTagset();
	}

	public TaggedWord(final String term, final String tag, Tagset tagset) {
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
		final var pos = taggedWord.lastIndexOf(tagset.getTagSeparator());
		return new TaggedWord(taggedWord.substring(0, pos), taggedWord.substring(pos), tagset);
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

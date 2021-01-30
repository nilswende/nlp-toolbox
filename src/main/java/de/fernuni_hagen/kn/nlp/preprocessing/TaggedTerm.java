package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.function.Function;

/**
 * A POS tagged term.
 *
 * @author Nils Wende
 */
public class TaggedTerm {

	private final String term;
	private final String tag;
	private final Tagset tagset;

	private TaggedTerm(final String term, final String tag, final Tagset tagset) {
		this.term = term;
		this.tag = tag;
		this.tagset = tagset;
	}

	/**
	 * Creates a TaggedTerm from a tagged term.
	 *
	 * @param taggedTerm a tagged term
	 * @param tagset     the tagset used for tagging
	 * @return TaggedTerm
	 */
	public static TaggedTerm from(final String taggedTerm, final Tagset tagset) {
		final var separator = tagset.getTagSeparator();
		final var pos = taggedTerm.lastIndexOf(separator);
		return new TaggedTerm(taggedTerm.substring(0, pos), taggedTerm.substring(pos + separator.length()), tagset);
	}

	/**
	 * Returns a copy of this TaggedTerm with the given term.
	 *
	 * @param term the new term
	 * @return TaggedTerm
	 */
	public TaggedTerm withTerm(final String term) {
		return new TaggedTerm(term, tag, tagset);
	}

	/**
	 * Returns a copy of this TaggedTerm with the mapper applied to the current term.
	 *
	 * @param mapper creating the new term from the old
	 * @return TaggedTerm
	 */
	public TaggedTerm withTerm(final Function<String, String> mapper) {
		return withTerm(mapper.apply(term));
	}

	/**
	 * Checks if this term is a noun.
	 *
	 * @return true, if this term is tagged as a noun
	 */
	public boolean isNoun() {
		return tag.startsWith(tagset.getNounTag());
	}

	/**
	 * Checks if this term is a proper noun.
	 *
	 * @return true, if this term is tagged as a proper noun
	 */
	public boolean isProperNoun() {
		return tag.startsWith(tagset.getProperNounTag());
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

package de.fernuni_hagen.kn.nlp.preprocessing;

/**
 * A POS tagged term.
 *
 * @author Nils Wende
 */
public class TaggedTerm {

	private final String term;
	private final String tag;
	private final Tagset tagset;

	public TaggedTerm(final String term, final TaggedTerm taggedTerm) {
		this.term = term;
		this.tag = taggedTerm.getTag();
		this.tagset = taggedTerm.getTagset();
	}

	public TaggedTerm(final String term, final String tag, final Tagset tagset) {
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

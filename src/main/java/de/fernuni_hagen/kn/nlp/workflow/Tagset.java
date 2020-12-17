package de.fernuni_hagen.kn.nlp.workflow;

/**
 * Identifies the noun tag of different tagsets.
 */
public enum Tagset {
	STTS("|", "|N");

	private final String tagSeparator;
	private final String nounTag;

	Tagset(final String tagSeparator, final String nounTag) {
		this.tagSeparator = tagSeparator;
		this.nounTag = nounTag;
	}

	public String getTagSeparator() {
		return tagSeparator;
	}

	public String getNounTag() {
		return nounTag;
	}

}

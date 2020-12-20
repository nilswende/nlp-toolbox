package de.fernuni_hagen.kn.nlp.preprocessing;

import java.nio.file.Path;

/**
 * Identifies the tagset of a tagger.
 *
 * @author Nils Wende
 */
public enum Tagset {
	STTS("stts", "|", "N", "NE"),
	BNC("bnc", "|", "N", "NP0");

	private final String lexicon;
	private final String tagList;
	private final String transitions;
	private final String tagSeparator;
	private final String nounTag;
	private final String properNounTag;

	Tagset(final String dir, final String tagSeparator, final String nounTag, String properNounTag) {
		this.tagSeparator = tagSeparator;
		this.nounTag = nounTag;
		this.properNounTag = properNounTag;
		final var path = Path.of("resources", "taggermodels", dir);
		lexicon = path.resolve(".lexicon").toString();
		tagList = path.resolve(".taglist").toString();
		transitions = path.resolve(".transitions").toString();
	}

	public String getLexicon() {
		return lexicon;
	}

	public String getTagList() {
		return tagList;
	}

	public String getTransitions() {
		return transitions;
	}

	public String getTagSeparator() {
		return tagSeparator;
	}

	public String getNounTag() {
		return nounTag;
	}

	public String getProperNounTag() {
		return properNounTag;
	}
}

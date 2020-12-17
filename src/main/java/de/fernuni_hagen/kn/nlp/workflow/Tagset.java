package de.fernuni_hagen.kn.nlp.workflow;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Identifies the tagset of a tagger.
 *
 * @author Nils Wende
 */
public enum Tagset {
	STTS("stts", "|", "|N"),
	BNC("bnc", "|", "|N");

	private final String lexicon;
	private final String taglist;
	private final String transitions;
	private final String tagSeparator;
	private final String nounTag;

	Tagset(final String dir, final String tagSeparator, final String nounTag) {
		this.tagSeparator = tagSeparator;
		this.nounTag = nounTag;
		final var path = Path.of("resources", "taggermodels", dir);
		lexicon = path.resolve(".lexicon").toString();
		taglist = path.resolve(".taglist").toString();
		transitions = path.resolve(".transitions").toString();
	}

	/**
	 * Maps a locale to its Tagset.
	 *
	 * @param locale contains the language
	 * @return the Tagset
	 */
	public static Tagset from(final Locale locale) {
		switch (locale.getLanguage()) {
			case "de":
				return STTS;
			case "en":
				return BNC;
			default:
				throw new IllegalArgumentException("Unsupported locale: " + locale);
		}
	}

	public String getLexicon() {
		return lexicon;
	}

	public String getTaglist() {
		return taglist;
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

}

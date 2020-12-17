package de.fernuni_hagen.kn.nlp.workflow;

import java.util.stream.Stream;

/**
 * Uses POS tagging on sentences.
 *
 * @author Nils Wende
 */
public interface Tagger {

	/**
	 * Uses POS tagging on sentences.
	 *
	 * @param sentence the sentence to be tagged
	 * @return the tagged sentence
	 */
	Stream<TaggedWord> tag(String sentence);

	/**
	 * Returns the tagset used by this tagger.
	 *
	 * @return Tagset
	 */
	Tagset getTagset();

}

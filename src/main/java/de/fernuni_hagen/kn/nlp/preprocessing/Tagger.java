package de.fernuni_hagen.kn.nlp.preprocessing;

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
	Stream<TaggedTerm> tag(String sentence);

}

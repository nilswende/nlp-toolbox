package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.List;

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
	List<TaggedTerm> tag(String sentence);

}

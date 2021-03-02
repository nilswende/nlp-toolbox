package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.List;
import java.util.function.Function;

/**
 * Uses POS tagging on sentences.
 *
 * @author Nils Wende
 */
public interface Tagger extends Function<String, List<TaggedTerm>> {

	/**
	 * Uses POS tagging on sentences.
	 *
	 * @param sentence the sentence to be tagged
	 * @return the tagged sentence
	 */
	List<TaggedTerm> apply(String sentence);

}

package de.fernuni_hagen.kn.nlp.workflow;

/**
 * Uses POS tagging on sentences.
 *
 * @author Nils Wende
 */
@FunctionalInterface
public interface Tagger {

	/**
	 * Uses POS tagging on sentences.
	 *
	 * @param sentence the sentence to be tagged
	 * @return the tagged sentence
	 */
	String tag(String sentence);

}

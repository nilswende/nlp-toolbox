package de.fernuni_hagen.kn.nlp.workflow;

import java.util.stream.Stream;

/**
 * Retains only nouns in sentences.
 *
 * @author Nils Wende
 */
public interface NounFilter {

	/**
	 * Retains only nouns in sentences.
	 *
	 * @param sentence the words of a sentence
	 * @return the sentence containing only nouns
	 */
	Stream<TaggedWord> filter(Stream<TaggedWord> sentence);

}

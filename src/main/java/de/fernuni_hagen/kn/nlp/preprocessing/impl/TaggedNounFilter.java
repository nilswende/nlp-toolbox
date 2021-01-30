package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.NounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.TaggedTerm;

import java.util.stream.Stream;

/**
 * Retains only nouns in tagged sentences.
 *
 * @author Nils Wende
 */
public class TaggedNounFilter implements NounFilter {

	@Override
	public Stream<TaggedTerm> apply(final Stream<TaggedTerm> sentence) {
		return sentence.filter(TaggedTerm::isNoun);
	}

}

package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.NounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.TaggedWord;
import de.fernuni_hagen.kn.nlp.preprocessing.Tagset;

import java.util.stream.Stream;

/**
 * Retains only nouns in sentences.
 *
 * @author Nils Wende
 */
public class NounFilterImpl implements NounFilter {

	@Override
	public Stream<TaggedWord> apply(final Stream<TaggedWord> sentence) {
		return sentence.filter(w -> isNoun(w.getTerm(), w.getTagset()));
	}

	private boolean isNoun(final String word, final Tagset tagset) {
		return word.lastIndexOf(tagset.getTagSeparator()) == word.lastIndexOf(tagset.getNounTag());
	}

}

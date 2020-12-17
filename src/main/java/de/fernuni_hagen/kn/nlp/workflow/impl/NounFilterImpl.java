package de.fernuni_hagen.kn.nlp.workflow.impl;

import de.fernuni_hagen.kn.nlp.workflow.NounFilter;
import de.fernuni_hagen.kn.nlp.workflow.TaggedWord;
import de.fernuni_hagen.kn.nlp.workflow.Tagset;

import java.util.stream.Stream;

/**
 * Retains only nouns in sentences.
 *
 * @author Nils Wende
 */
public class NounFilterImpl implements NounFilter {

	@Override
	public Stream<TaggedWord> filter(final Stream<TaggedWord> sentence) {
		return sentence.filter(w -> isNoun(w.getTerm(), w.getTagset()));
	}

	private boolean isNoun(final String word, final Tagset tagset) {
		return word.lastIndexOf(tagset.getTagSeparator()) == word.lastIndexOf(tagset.getNounTag());
	}

}

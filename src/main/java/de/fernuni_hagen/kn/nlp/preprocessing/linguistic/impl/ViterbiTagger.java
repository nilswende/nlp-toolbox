package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.TaggedTerm;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Tagger;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Tagset;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Uses POS tagging on sentences.
 *
 * @author Nils Wende
 */
public class ViterbiTagger implements Tagger {

	private final Tagset tagset;
	private final de.uni_leipzig.asv.toolbox.viterbitagger.Tagger tagger;

	public ViterbiTagger(final Tagset tagset) {
		this.tagset = tagset;
		tagger = new de.uni_leipzig.asv.toolbox.viterbitagger.Tagger(
				tagset.getTagList(),
				tagset.getLexicon(),
				tagset.getTransitions(),
				false);
	}

	@Override
	public List<TaggedTerm> apply(final String sentence) {
		final var taggedSentence = tagger.tagSentence(sentence).stripLeading();
		final var terms = taggedSentence.split(StringUtils.SPACE);
		return Arrays.stream(terms)
				.map(term -> TaggedTerm.from(term, tagset))
				.collect(Collectors.toList());
	}

}

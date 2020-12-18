package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.TaggedWord;
import de.fernuni_hagen.kn.nlp.preprocessing.Tagger;
import de.fernuni_hagen.kn.nlp.preprocessing.Tagset;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Stream;

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
	public Stream<TaggedWord> tag(final String sentence) {
		final var taggedSentence = tagger.tagSentence(sentence).stripLeading();
		final var taggedWords = Arrays.stream(taggedSentence.split(StringUtils.SPACE));
		return TaggedWord.from(taggedWords, tagset);
	}

}

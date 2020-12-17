package de.fernuni_hagen.kn.nlp.workflow.impl;

import de.fernuni_hagen.kn.nlp.workflow.TaggedWord;
import de.fernuni_hagen.kn.nlp.workflow.Tagger;
import de.fernuni_hagen.kn.nlp.workflow.Tagset;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Uses POS tagging on sentences.
 *
 * @author Nils Wende
 */
public class ViterbiTagger implements Tagger {

	private final Tagset tagset;
	private final de.uni_leipzig.asv.toolbox.viterbitagger.Tagger tagger;

	public ViterbiTagger(final Locale locale) {
		tagset = Tagset.from(locale);
		tagger = new de.uni_leipzig.asv.toolbox.viterbitagger.Tagger(
				tagset.getTaglist(),
				tagset.getLexicon(),
				tagset.getTransitions(),
				false);
	}

	@Override
	public Stream<TaggedWord> tag(final String sentence) {
		final var taggedSentence = tagger.tagSentence(sentence);
		final var taggedWords = Arrays.stream(taggedSentence.split(StringUtils.SPACE));
		return TaggedWord.from(taggedWords, tagset);
	}

}

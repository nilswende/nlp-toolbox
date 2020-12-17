package de.fernuni_hagen.kn.nlp.workflow.impl;

import de.fernuni_hagen.kn.nlp.workflow.TaggedWord;
import de.fernuni_hagen.kn.nlp.workflow.Tagger;
import de.fernuni_hagen.kn.nlp.workflow.Tagset;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Uses POS tagging on sentences.
 *
 * @author Nils Wende
 */
public class ViterbiTagger implements Tagger {

	private final de.uni_leipzig.asv.toolbox.viterbitagger.Tagger tagger;

	public ViterbiTagger(final Locale locale) {
		final var dir = Path.of("resources", "taggermodels", locale.getLanguage());
		tagger = new de.uni_leipzig.asv.toolbox.viterbitagger.Tagger(
				dir.resolve(".taglist").toString(),
				dir.resolve(".lexicon").toString(),
				dir.resolve(".transitions").toString(),
				false);
	}

	@Override
	public Stream<TaggedWord> tag(final String sentence) {
		final var taggedSentence = tagger.tagSentence(sentence);
		final var taggedWords = Arrays.stream(taggedSentence.split(StringUtils.SPACE));
		return TaggedWord.from(taggedWords, getTagset());
	}

	@Override
	public Tagset getTagset() {
		return Tagset.STTS;
	}

}

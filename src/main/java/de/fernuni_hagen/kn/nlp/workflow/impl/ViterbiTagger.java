package de.fernuni_hagen.kn.nlp.workflow.impl;

import de.fernuni_hagen.kn.nlp.workflow.Tagger;

import java.nio.file.Path;
import java.util.Locale;

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
	public String tag(final String sentence) {
		return tagger.tagSentence(sentence);
	}

}

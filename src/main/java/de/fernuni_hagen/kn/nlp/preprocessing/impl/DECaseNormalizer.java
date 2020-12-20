package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.CaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.TaggedWord;

import java.util.Locale;
import java.util.stream.Stream;

/**
 * Normalizes the case of a german word.
 *
 * @author Nils Wende
 */
public class DECaseNormalizer implements CaseNormalizer {

	@Override
	public Stream<TaggedWord> apply(final Stream<TaggedWord> sentence) {
		return sentence.map(w -> w.isNoun() ? w : normalizeCase(w));
	}

	private TaggedWord normalizeCase(final TaggedWord w) {
		return new TaggedWord(w.getTerm().toLowerCase(Locale.GERMAN), w);
	}

}

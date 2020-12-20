package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.CaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.TaggedWord;

import java.util.Locale;
import java.util.stream.Stream;

/**
 * Normalizes the case of an english word.
 *
 * @author Nils Wende
 */
public class ENCaseNormalizer implements CaseNormalizer {

	@Override
	public Stream<TaggedWord> apply(final Stream<TaggedWord> sentence) {
		return sentence.map(w -> w.isProperNoun() ? w : normalizeCase(w));
	}

	private TaggedWord normalizeCase(TaggedWord w) {
		return new TaggedWord(w.getTerm().toLowerCase(Locale.ENGLISH), w);
	}

}

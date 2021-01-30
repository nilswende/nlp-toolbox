package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.CaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.TaggedTerm;

import java.util.Locale;
import java.util.stream.Stream;

/**
 * Normalizes the case of an english word.
 *
 * @author Nils Wende
 */
public class ENCaseNormalizer implements CaseNormalizer {

	@Override
	public Stream<TaggedTerm> apply(final Stream<TaggedTerm> sentence) {
		return sentence.map(w -> w.isProperNoun() ? w : normalizeCase(w));
	}

	private TaggedTerm normalizeCase(TaggedTerm w) {
		return new TaggedTerm(w.getTerm().toLowerCase(Locale.ENGLISH), w);
	}

}

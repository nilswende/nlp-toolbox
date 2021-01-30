package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.CaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.TaggedTerm;

import java.util.Locale;
import java.util.stream.Stream;

/**
 * Normalizes the case of a german word.
 *
 * @author Nils Wende
 */
public class DECaseNormalizer implements CaseNormalizer {

	@Override
	public Stream<TaggedTerm> apply(final Stream<TaggedTerm> sentence) {
		return sentence.map(w -> w.isNoun() ? w : normalizeCase(w));
	}

	private TaggedTerm normalizeCase(final TaggedTerm w) {
		return new TaggedTerm(w.getTerm().toLowerCase(Locale.GERMAN), w);
	}

}

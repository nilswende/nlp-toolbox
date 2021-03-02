package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.CaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.TaggedTerm;

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
		return sentence.map(t -> t.isNoun() ? t : normalizeCase(t));
	}

	private TaggedTerm normalizeCase(final TaggedTerm tagged) {
		return tagged.withTerm(t -> t.toLowerCase(Locale.GERMAN));
	}

}

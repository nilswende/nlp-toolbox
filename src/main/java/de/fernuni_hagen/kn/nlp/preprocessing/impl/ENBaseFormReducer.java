package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.BaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.TaggedTerm;
import te.utils.Porter;

import java.util.stream.Stream;

/**
 * Reduces the terms of an english sentence to their base forms.
 *
 * @author Nils Wende
 */
public class ENBaseFormReducer implements BaseFormReducer {

	private final Porter reducer = new Porter();

	@Override
	public Stream<TaggedTerm> apply(final Stream<TaggedTerm> sentence) {
		return sentence.map(t -> t.withTerm(reducer::stem));
	}

}

package de.fernuni_hagen.kn.nlp.workflow.impl;

import de.fernuni_hagen.kn.nlp.workflow.BaseFormReducer;
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
	public Stream<String> reduce(final Stream<String> sentence) {
		return sentence.map(reducer::stem);
	}

}

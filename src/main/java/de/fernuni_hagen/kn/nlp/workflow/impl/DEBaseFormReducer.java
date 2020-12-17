package de.fernuni_hagen.kn.nlp.workflow.impl;

import de.fernuni_hagen.kn.nlp.workflow.BaseFormReducer;
import de.uni_leipzig.asv.toolbox.baseforms.Zerleger2;

import java.util.stream.Stream;

/**
 * Reduces the terms of a german sentence to their base forms.
 *
 * @author Nils Wende
 */
public class DEBaseFormReducer implements BaseFormReducer {

	private final Zerleger2 reducer = new Zerleger2();

	@Override
	public Stream<String> reduce(Stream<String> sentence) {
		return sentence.map(reducer::grundFormReduktion);
	}

}

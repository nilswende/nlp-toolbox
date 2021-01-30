package de.fernuni_hagen.kn.nlp.preprocessing;

import java.util.Collections;
import java.util.List;

/**
 * A sentence containing tagged terms and phrases.
 *
 * @author Nils Wende
 */
public class Sentence {

	private final List<TaggedTerm> terms;
	private final List<String> phrases;

	public Sentence(final List<TaggedTerm> terms, final List<String> phrases) {
		this.terms = terms;
		this.phrases = phrases;
	}

	public Sentence(final List<TaggedTerm> terms) {
		this(terms, Collections.emptyList());
	}

	public List<TaggedTerm> getTerms() {
		return terms;
	}

	public List<String> getPhrases() {
		return phrases;
	}

}

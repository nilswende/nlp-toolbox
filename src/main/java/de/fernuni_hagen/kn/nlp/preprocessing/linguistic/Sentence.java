package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A sentence containing tagged terms.
 *
 * @author Nils Wende
 */
public class Sentence {

	/**
	 * The tagged terms.
	 */
	protected final List<TaggedTerm> terms;

	/**
	 * Create a sentence.
	 *
	 * @param terms TaggedTerms
	 */
	public Sentence(final List<TaggedTerm> terms) {
		this.terms = List.copyOf(terms);
	}

	/**
	 * Returns a copy of this Sentence with the mapper applied to its terms.
	 *
	 * @param mapper creating the new terms from the old ones
	 * @return Sentence
	 */
	public Sentence withTerms(final UnaryOperator<Stream<TaggedTerm>> mapper) {
		final var newTerms = mapTerms(mapper);
		return new Sentence(newTerms);
	}

	/**
	 * Apply the mapper to the terms.
	 *
	 * @param mapper creating the new terms from the old ones
	 * @return transformed terms
	 */
	final List<TaggedTerm> mapTerms(final UnaryOperator<Stream<TaggedTerm>> mapper) {
		return mapper.apply(terms.stream()).collect(Collectors.toList());
	}

	/**
	 * Returns all textual sentence content, such as terms.
	 *
	 * @return Stream
	 */
	public List<String> getContent() {
		return terms.stream().map(TaggedTerm::getTerm).collect(Collectors.toList());
	}

}

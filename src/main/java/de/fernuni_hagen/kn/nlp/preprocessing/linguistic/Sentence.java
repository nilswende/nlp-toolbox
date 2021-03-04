package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A sentence containing tagged terms and phrases.
 *
 * @author Nils Wende
 */
public class Sentence {

	private final List<TaggedTerm> terms;
	private final List<String> phrases;

	/**
	 * Create a sentence including phrases.
	 *
	 * @param terms   TaggedTerms
	 * @param phrases phrases
	 */
	public Sentence(final List<TaggedTerm> terms, final List<String> phrases) {
		this.terms = List.copyOf(terms);
		this.phrases = List.copyOf(phrases);
	}

	/**
	 * Create a sentence without phrases.
	 *
	 * @param terms TaggedTerms
	 */
	public Sentence(final List<TaggedTerm> terms) {
		this(terms, List.of());
	}

	/**
	 * Returns a copy of this Sentence with the mapper applied to its terms.
	 *
	 * @param mapper creating the new terms from the old
	 * @return Sentence
	 */
	public Sentence withTerms(final UnaryOperator<Stream<TaggedTerm>> mapper) {
		final var newTerms = mapper.apply(terms.stream()).collect(Collectors.toList());
		return new Sentence(newTerms, phrases);
	}

	/**
	 * Returns all textual sentence content, such as terms and phrases.
	 *
	 * @return Stream
	 */
	public Stream<String> getContent() {
		return Stream.concat(terms.stream().map(TaggedTerm::getTerm), phrases.stream());
	}

}

package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A sentence containing tagged terms and phrases.
 *
 * @author Nils Wende
 */
public class PhrasedSentence extends Sentence {

	private final Map<Integer, String> phrases;

	/**
	 * Create a sentence including phrases.
	 *
	 * @param terms   TaggedTerms
	 * @param phrases phrases
	 */
	public PhrasedSentence(final List<TaggedTerm> terms, final Map<Integer, String> phrases) {
		super(terms);
		this.phrases = Map.copyOf(phrases);
	}

	/**
	 * Returns a copy of this Sentence with the mapper applied to its terms.
	 *
	 * @param mapper creating the new terms from the old ones
	 * @return Sentence
	 */
	@Override
	public PhrasedSentence withTerms(final UnaryOperator<Stream<TaggedTerm>> mapper) {
		final var newTerms = mapTerms(mapper);
		return new PhrasedSentence(newTerms, phrases);
	}

	/**
	 * Returns all textual sentence content, such as terms and phrases.
	 *
	 * @return Stream
	 */
	@Override
	public Stream<String> getContent() {
		final var list = new ArrayList<String>();
		int start = 0;
		for (final Map.Entry<Integer, String> entry : phrases.entrySet()) {
			final int pos = entry.getKey();
			final String phrase = entry.getValue();
			terms.subList(start, pos).stream().map(TaggedTerm::getTerm).forEach(list::add);
			list.add(phrase);
			start = pos;
		}
		terms.subList(start, terms.size()).stream().map(TaggedTerm::getTerm).forEach(list::add);
		return list.stream();
	}

}

package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A sentence containing tagged terms and phrases.
 *
 * @author Nils Wende
 */
public class PhrasedSentence extends Sentence {

	private final String sentence;
	private final List<String> phrases;

	/**
	 * Create a sentence including phrases.
	 *
	 * @param terms    TaggedTerms
	 * @param sentence original sentence
	 * @param phrases  phrases
	 */
	public PhrasedSentence(final List<TaggedTerm> terms, final String sentence, final List<String> phrases) {
		super(terms);
		this.sentence = sentence;
		this.phrases = List.copyOf(phrases);
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
		return new PhrasedSentence(newTerms, sentence, phrases);
	}

	/**
	 * Returns all textual sentence content, such as terms and phrases.
	 *
	 * @return Stream
	 */
	@Override
	public Stream<String> getContent() {
		if (phrases.isEmpty()) {
			return super.getContent();
		}
		final var list = new ArrayList<String>();
		int start = 0;
		final var phraseIterator = new PhraseIterator(sentence, phrases);
		while (phraseIterator.hasNext()) {
			final var phrase = phraseIterator.next();
			final var position = phraseIterator.position();

			final TaggedTerm term = lastTermBefore(position);
			if (term != null) {
				final var pos = term.getPosition() + 1;
				addTerms(list, start, pos);
				start = pos;
			}
			list.add(phrase);
		}
		addTerms(list, start, terms.size());
		return list.stream();
	}

	private TaggedTerm lastTermBefore(final int position) {
		TaggedTerm term = null;
		int max = 0;
		for (final TaggedTerm t : terms) {
			final var i = sentence.lastIndexOf(t.getTerm(), position);
			if (i != -1 && i >= max) {
				term = t;
				max = i;
			}
		}
		return term;
	}

	private void addTerms(final List<String> list, final int start, final int pos) {
		terms.subList(start, pos).stream().map(TaggedTerm::getTerm).forEach(list::add);
	}

}

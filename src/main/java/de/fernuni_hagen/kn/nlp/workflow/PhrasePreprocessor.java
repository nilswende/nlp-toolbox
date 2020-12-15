package de.fernuni_hagen.kn.nlp.workflow;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing of a document, including phrase extraction.
 *
 * @author Nils Wende
 */
class PhrasePreprocessor extends Preprocessor {

	// Decorator
	@Override
	protected Stream<List<String>> processSentences(final Stream<String> sentences, final Locale locale) {
		final var pairs = new IndexerPhraseExtractor().extractPhrases(locale, sentences.collect(Collectors.toList()));
		final var iterator = pairs.iterator();

		return super.processSentences(pairs.stream().map(Pair::getLeft), locale)
				// re-add the extracted phrases
				.map(l -> Stream.of(l, iterator.next().getRight()).flatMap(List::stream).collect(Collectors.toList()));
	}

}

package de.fernuni_hagen.kn.nlp.workflow;

import de.fernuni_hagen.kn.nlp.workflow.impl.IndexerPhraseExtractor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing of a document, including phrase extraction.
 *
 * @author Nils Wende
 */
class PhrasePreprocessor extends Preprocessor {

	PhrasePreprocessor(final List<Function<Locale, WorkflowStep>> workflowSteps) {
		super(workflowSteps);
	}

	//TODO Decorator, extract interface?
	@Override
	protected Stream<List<String>> processSentences(final Stream<String> sentences, final Locale locale) {
		final var pairs = new IndexerPhraseExtractor().extractPhrases(locale, sentences.collect(Collectors.toList()));
		final var iterator = pairs.iterator();
		// exclude the phrases from further processing
		return super.processSentences(pairs.stream().map(Pair::getLeft), locale)
				// include the phrases in the final result
				.map(l -> Stream.of(l, iterator.next().getRight()).flatMap(List::stream).collect(Collectors.toList()));
	}

}

package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.preprocessing.factory.PreprocessingFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing of a document, including phrase extraction.
 *
 * @author Nils Wende
 */
class PhrasePreprocessor extends Preprocessor {

	PhrasePreprocessor(final List<Function<PreprocessingFactory, PreprocessingStep>> workflowSteps) {
		super(workflowSteps);
	}

	@Override
	protected Stream<List<String>> processSentences(final Stream<String> sentences, final PreprocessingFactory factory) {
		final List<Pair<String, List<String>>> pairs = factory.createPhraseExtractor().extractPhrases(sentences.collect(Collectors.toList()));
		final var iterator = pairs.iterator();
		// exclude the phrases from further processing
		return super.processSentences(pairs.stream().map(Pair::getLeft), factory)
				// ensure sequential-ness to safely use the iterator
				.sequential()
				// include the phrases in the final result
				.map(l -> Stream.of(l, iterator.next().getRight()).flatMap(List::stream).collect(Collectors.toList()));
	}

}

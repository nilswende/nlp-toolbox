package de.fernuni_hagen.kn.nlp.workflow;

import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.input.SimpleSentenceExtractor;
import de.fernuni_hagen.kn.nlp.input.impl.JLanILanguageExtractor;
import de.fernuni_hagen.kn.nlp.input.impl.RegexWhitespaceRemover;
import de.fernuni_hagen.kn.nlp.workflow.impl.ViterbiTagger;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing of a document.
 *
 * @author Nils Wende
 */
public class Preprocessor {

	/**
	 * Executes the linguistic preprocessing of a document.
	 *
	 * @param document the document to be processed
	 * @return stream of the sentences inside the document, split into words
	 */
	public Stream<List<String>> preprocess(final File document) {
		final var locale = new JLanILanguageExtractor().extract(document);
		final var sentenceExtractor = new SimpleSentenceExtractor(locale, new RegexWhitespaceRemover());
		// file level
		final var sentences = sentenceExtractor.extract(document).collect(Collectors.toList());
		return processSentences(sentences.stream(), locale);
	}

	protected Stream<List<String>> processSentences(final Stream<String> sentences, final Locale locale) {
		final var tagger = new ViterbiTagger(locale);
		return sentences
				.map(tagger::tag)
				.map(s -> Arrays.asList(s.split(" ")));
	}

	/**
	 * Creates a new preprocessor from the given config.
	 *
	 * @param config Config
	 * @return a new preprocessor
	 */
	public static Preprocessor from(final Config config) {
		return config.extractPhrases() ? new PhrasePreprocessor() : new Preprocessor();
	}

}

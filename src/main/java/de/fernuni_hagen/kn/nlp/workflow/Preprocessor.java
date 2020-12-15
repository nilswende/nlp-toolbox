package de.fernuni_hagen.kn.nlp.workflow;

import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.input.LanguageExtractor;
import de.fernuni_hagen.kn.nlp.input.SimpleSentenceExtractor;
import de.fernuni_hagen.kn.nlp.input.impl.JLanILanguageExtractor;
import de.fernuni_hagen.kn.nlp.input.impl.RegexWhitespaceRemover;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing of a document.
 *
 * @author Nils Wende
 */
public class Preprocessor {

	private final LanguageExtractor languageExtractor;
	private final PhraseExtractor phraseExtractor;

	public Preprocessor(final PhraseExtractor phraseExtractor) {
		languageExtractor = new JLanILanguageExtractor();
		this.phraseExtractor = phraseExtractor;
	}

	public Stream<List<String>> preprocess(final File document) {
		final var locale = languageExtractor.extract(document);
		final var sentenceExtractor = new SimpleSentenceExtractor(locale, new RegexWhitespaceRemover());
		// file level
		final var sentences = sentenceExtractor.extract(document).collect(Collectors.toList());
		final var pairs = phraseExtractor.extractPhrases(locale, sentences); //TODO fast track no phrase extraction?

		final Iterator<Pair<String, List<String>>> iterator = pairs.iterator();
		return pairs.stream().map(Pair::getLeft)
				// sentence level
				.map(s -> Arrays.asList(s.split(" ")))
				// re-add the extracted phrases
				.map(l -> Stream.of(l, iterator.next().getRight()).flatMap(List::stream).collect(Collectors.toList()));
	}

	public static Preprocessor from(final Config config) {
		return new Preprocessor(config.extractPhrases() ? new IndexerPhraseExtractor() : new NoOpPhraseExtractor());
	}

}

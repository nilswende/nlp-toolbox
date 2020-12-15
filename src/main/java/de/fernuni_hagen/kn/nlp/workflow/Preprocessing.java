package de.fernuni_hagen.kn.nlp.workflow;

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
 * Führt die linguistische Vorverarbeitung des Dokuments durch.
 *
 * @author Nils Wende
 */
public class Preprocessing {

	public Stream<List<String>> preprocess(final File document) {
		final var locale = new JLanILanguageExtractor().extract(document);
		final var sentenceExtractor = new SimpleSentenceExtractor(locale, new RegexWhitespaceRemover());
		// file level
		final var sentences = sentenceExtractor.extract(document).collect(Collectors.toList());
		final var pairs = new IndexerPhraseExtractor(locale).extractPhrases(sentences);

		final Iterator<Pair<String, List<String>>> iterator = pairs.iterator();
		return pairs.stream().map(Pair::getLeft)
				// sentence level
				.map(s -> Arrays.asList(s.split(" ")))
				// re-add the extracted phrases
				.map(l -> Stream.of(l, iterator.next().getRight()).flatMap(List::stream).collect(Collectors.toList()));
	}

}

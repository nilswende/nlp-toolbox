package de.fernuni_hagen.kn.nlp.workflow;

import de.fernuni_hagen.kn.nlp.input.SimpleSentenceExtractor;
import de.fernuni_hagen.kn.nlp.input.impl.JLanILanguageExtractor;
import de.fernuni_hagen.kn.nlp.input.impl.RegexWhitespaceRemover;

import java.io.File;
import java.util.Arrays;
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
		final var sentenceExtractor = new SimpleSentenceExtractor(new JLanILanguageExtractor(), new RegexWhitespaceRemover());
		// file level
		final var sentences = sentenceExtractor.extract(document).collect(Collectors.toList());
		return sentences.stream()
				// sentence level
				.map(s -> Arrays.asList(s.split(" ")));
	}

}

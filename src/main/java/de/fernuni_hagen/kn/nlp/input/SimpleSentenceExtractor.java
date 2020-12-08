package de.fernuni_hagen.kn.nlp.input;

import de.fernuni_hagen.kn.nlp.SentenceExtractor;

import java.io.File;
import java.util.stream.Stream;

/**
 * Extracts sentences from a text file.
 *
 * @author Nils Wende
 */
public class SimpleSentenceExtractor implements SentenceExtractor {

	private final LanguageExtractor languageExtractor;

	public SimpleSentenceExtractor(final LanguageExtractor languageExtractor) {
		this.languageExtractor = languageExtractor;
	}

	@Override
	public Stream<String> extract(final File textFile) {
		final var locale = languageExtractor.extract(textFile);
		return null;
	}

}

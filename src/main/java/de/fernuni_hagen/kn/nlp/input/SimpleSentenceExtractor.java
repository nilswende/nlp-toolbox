package de.fernuni_hagen.kn.nlp.input;

import de.fernuni_hagen.kn.nlp.SentenceExtractor;
import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Extracts sentences from a text file using {@link java.text.BreakIterator}.
 *
 * @author Nils Wende
 */
public class SimpleSentenceExtractor implements SentenceExtractor {

	private final Config config;
	private final LanguageExtractor languageExtractor;
	private final WhitespaceRemover whitespaceRemover;

	public SimpleSentenceExtractor(final Config config, final LanguageExtractor languageExtractor, final WhitespaceRemover whitespaceRemover) {
		this.config = config;
		this.languageExtractor = languageExtractor;
		this.whitespaceRemover = whitespaceRemover;
	}

	@Override
	public Stream<String> extract(final File textFile) {
		try (final var sentenceSupplier = new LazySentenceSupplier(textFile, getLocale(textFile), whitespaceRemover)) {
			final var sentences = new ArrayList<String>();
			for (String s; (s = sentenceSupplier.get()) != null; ) {
				sentences.add(s);
			}
			return sentences.stream();
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private Locale getLocale(final File textFile) {
		final var availableLocales = Arrays.asList(BreakIterator.getAvailableLocales());
		final var language = languageExtractor.extract(textFile);
		final var priorityList = Collections.singletonList(new Locale.LanguageRange(language.toLanguageTag()));
		final var bestMatch = Locale.lookup(priorityList, availableLocales);
		if (bestMatch == null) {
			throw new IllegalArgumentException("BreakIterator does not support language " + language);
		}
		return bestMatch;
	}

}

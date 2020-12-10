package de.fernuni_hagen.kn.nlp.input;

import de.fernuni_hagen.kn.nlp.SentenceExtractor;
import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.input.impl.BufferedFileCharacterIterator;
import de.fernuni_hagen.kn.nlp.input.impl.BufferedFileReader;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
		try (final var iter = new BufferedFileCharacterIterator(new BufferedFileReader(textFile, Config.DEFAULT_CHARSET))) {
			final BreakIterator boundary = getBreakIterator(textFile);
			boundary.setText(iter);
			return extractSentences(textFile, boundary).stream();
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private BreakIterator getBreakIterator(final File textFile) {
		final Locale locale = getLocale(textFile);
		return BreakIterator.getSentenceInstance(locale);
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

	private List<String> extractSentences(final File textFile, final BreakIterator boundary) throws IOException {
		try (final var fileReader = new BufferedFileReader(textFile, Config.DEFAULT_CHARSET)) {
			final var sentences = new ArrayList<String>();
			int start = boundary.first();
			for (int end = boundary.next();
				 end != BreakIterator.DONE;
				 start = end, end = boundary.next()) {
				sentences.add(extractSentence(fileReader, start, end));
			}
			return sentences;
		}
	}

	private String extractSentence(final BufferedFileReader fileReader, final int start, final int end) {
		final var chars = fileReader.read(start, end - start);
		return whitespaceRemover.removeWhitespace(CharBuffer.wrap(chars));
	}

}

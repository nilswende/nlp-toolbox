package de.fernuni_hagen.kn.nlp.input;

import de.fernuni_hagen.kn.nlp.preprocessing.SentenceExtractor;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Extracts sentences from a text file using a {@link java.text.BreakIterator}.
 *
 * @author Nils Wende
 */
public class SimpleSentenceExtractor implements SentenceExtractor {

	private final Locale locale;
	private final WhitespaceRemover whitespaceRemover;

	public SimpleSentenceExtractor(final Locale locale, final WhitespaceRemover whitespaceRemover) {
		this.locale = locale;
		this.whitespaceRemover = whitespaceRemover;
	}

	@Override
	public Stream<String> extract(final File textFile) {
		final var sentenceSupplier = new LazySentenceSupplier(textFile, findBestMatch(locale));
		return Stream.iterate(extractOneSentence(sentenceSupplier),
				Objects::nonNull,
				s -> extractOneSentence(sentenceSupplier))
				.onClose(() -> close(sentenceSupplier));
	}

	private Locale findBestMatch(final Locale locale) {
		final var availableLocales = List.of(BreakIterator.getAvailableLocales());
		final var priorityList = List.of(new Locale.LanguageRange(locale.toLanguageTag()));
		final var bestMatch = Locale.lookup(priorityList, availableLocales);
		if (bestMatch == null) {
			throw new IllegalArgumentException("BreakIterator does not support locale " + locale);
		}
		return bestMatch;
	}

	private String extractOneSentence(final LazySentenceSupplier sentenceSupplier) {
		final var chars = sentenceSupplier.get();
		return chars == null ? null : whitespaceRemover.removeWhitespace(CharBuffer.wrap(chars));
	}

	private void close(final LazySentenceSupplier supplier) {
		try {
			supplier.close();
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

}

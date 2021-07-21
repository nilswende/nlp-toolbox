package de.fernuni_hagen.kn.nlp.preprocessing.textual;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.factory.SentenceExtractor;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Extracts sentences from a text file using a {@link LazySentenceSupplier}.
 *
 * @author Nils Wende
 */
public class SimpleSentenceExtractor implements SentenceExtractor {

	private final Locale locale;
	private final SentenceSplitter sentenceSplitter;
	private Iterator<String> sentences;

	public SimpleSentenceExtractor(final Locale locale, final SentenceSplitter sentenceSplitter) {
		this.locale = locale;
		this.sentenceSplitter = sentenceSplitter;
	}

	@Override
	public Stream<String> extract(final Path textFile) {
		final var sentenceSupplier = new LazySentenceSupplier(textFile, locale);
		return Stream.iterate(extractOneSentence(sentenceSupplier),
				Objects::nonNull,
				s -> extractOneSentence(sentenceSupplier))
				.onClose(() -> close(sentenceSupplier));
	}

	private String extractOneSentence(final LazySentenceSupplier sentenceSupplier) {
		if (sentences == null) {
			final var chars = sentenceSupplier.get();
			if (chars == null) {
				return null;
			}
			sentences = sentenceSplitter.split(CharBuffer.wrap(chars)).iterator();
		}
		if (sentences.hasNext()) {
			return sentences.next();
		}
		sentences = null;
		return extractOneSentence(sentenceSupplier);
	}

	private void close(final LazySentenceSupplier supplier) {
		try {
			supplier.close();
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

}

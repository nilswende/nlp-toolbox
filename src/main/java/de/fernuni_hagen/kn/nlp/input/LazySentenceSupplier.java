package de.fernuni_hagen.kn.nlp.input;

import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.input.impl.BufferedFileCharacterIterator;
import de.fernuni_hagen.kn.nlp.input.impl.BufferedFileReader;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

/**
 * Reads a text file one sentence at a time.
 *
 * @author Nils Wende
 */
class LazySentenceSupplier implements Closeable {

	private final BufferedFileReader fileReader;
	private final BufferedFileCharacterIterator iter;
	private final BreakIterator boundary;
	private int start, end;

	/**
	 * Constructor.
	 *
	 * @param textFile the text file to be read
	 * @param locale   the file's language
	 */
	public LazySentenceSupplier(final Path textFile, final Locale locale) {
		fileReader = new BufferedFileReader(textFile, Config.DEFAULT_CHARSET);
		iter = new BufferedFileCharacterIterator(new BufferedFileReader(textFile, Config.DEFAULT_CHARSET));
		boundary = BreakIterator.getSentenceInstance(findBestMatch(locale));
		boundary.setText(iter);
		start = boundary.first();
		end = boundary.next();
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

	/**
	 * Reads and returns the next sentence.
	 *
	 * @return the next sentence or null, if the end of file was reached
	 */
	public char[] get() {
		if (end == BreakIterator.DONE) {
			return null;
		}
		final var chars = fileReader.read(start, end - start);
		start = end;
		end = boundary.next();
		return chars;
	}

	@Override
	public void close() throws IOException {
		iter.close();
		fileReader.close();
	}

}

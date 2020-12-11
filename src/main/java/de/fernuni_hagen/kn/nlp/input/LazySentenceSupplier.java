package de.fernuni_hagen.kn.nlp.input;

import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.input.impl.BufferedFileCharacterIterator;
import de.fernuni_hagen.kn.nlp.input.impl.BufferedFileReader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.text.BreakIterator;
import java.util.Locale;

/**
 * Reads a text file one sentence at a time.
 *
 * @author Nils Wende
 */
public class LazySentenceSupplier implements Closeable {

	private final WhitespaceRemover whitespaceRemover;
	private final BufferedFileCharacterIterator iter;
	private final BreakIterator boundary;
	private final BufferedFileReader fileReader;
	private int start, end;

	/**
	 * Constructor.
	 *
	 * @param textFile          the text file to be read
	 * @param locale            the file's language
	 * @param whitespaceRemover WhitespaceRemover
	 */
	public LazySentenceSupplier(final File textFile, final Locale locale, final WhitespaceRemover whitespaceRemover) {
		this.whitespaceRemover = whitespaceRemover;
		iter = new BufferedFileCharacterIterator(new BufferedFileReader(textFile, Config.DEFAULT_CHARSET));
		boundary = BreakIterator.getSentenceInstance(locale);
		boundary.setText(iter);
		fileReader = new BufferedFileReader(textFile, Config.DEFAULT_CHARSET);
		start = boundary.first();
		end = boundary.next();
	}

	/**
	 * Reads and returns the next sentence.
	 *
	 * @return the next sentence or null, if the end of file was reached
	 */
	public String get() {
		if (end == BreakIterator.DONE) {
			return null;
		}
		final var chars = fileReader.read(start, end - start);
		start = end;
		end = boundary.next();
		return whitespaceRemover.removeWhitespace(CharBuffer.wrap(chars));
	}

	@Override
	public void close() throws IOException {
		iter.close();
		fileReader.close();
	}

}

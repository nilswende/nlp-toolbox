package de.fernuni_hagen.kn.nlp.input;

import de.fernuni_hagen.kn.nlp.SentenceExtractor;
import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.text.BufferedFileCharacterIterator;
import de.fernuni_hagen.kn.nlp.text.BufferedFileReader;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Extracts sentences from a text file using {@link java.text.BreakIterator}.
 *
 * @author Nils Wende
 */
public class SimpleSentenceExtractor implements SentenceExtractor {

	private static final Pattern VERTICAL_WHITESPACE = Pattern.compile("\\v");
	private static final Pattern HORIZONTAL_WHITESPACE = Pattern.compile("\\h{2,}");
	private static final String SPACE = " ";

	private final Config config;
	private final LanguageExtractor languageExtractor;

	public SimpleSentenceExtractor(final Config config, final LanguageExtractor languageExtractor) {
		this.config = config;
		this.languageExtractor = languageExtractor;
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

	private List<String> extractSentences(final File textFile, final BreakIterator boundary) throws IOException {
		try (final var fileReader = new BufferedFileReader(textFile, Config.DEFAULT_CHARSET)) {
			final var sentences = new ArrayList<String>();
			int start = boundary.first();
			for (int end = boundary.next();
				 end != BreakIterator.DONE;
				 start = end, end = boundary.next()) {
				sentences.add(extractSentence(fileReader, start, end));
			}
			return sentences; //TODO lazily populate stream?
		}
	}

	private BreakIterator getBreakIterator(final File textFile) {
		final var availableLocales = Arrays.asList(BreakIterator.getAvailableLocales());
		final var language = languageExtractor.extract(textFile);
		if (!availableLocales.contains(language)) {
			throw new IllegalArgumentException("BreakIterator does not support language " + language);
		}
		return BreakIterator.getSentenceInstance(language);
	}

	private String extractSentence(final BufferedFileReader fileReader, final int start, final int end) {
		final var chars = fileReader.read(start, end - start);
		return stripWhitespace(CharBuffer.wrap(chars));
	}

	private String stripWhitespace(final CharSequence chars) {
		final var vStripped = VERTICAL_WHITESPACE.matcher(chars).replaceAll(SPACE);
		final var hStripped = HORIZONTAL_WHITESPACE.matcher(vStripped).replaceAll(SPACE);
		return hStripped.stripTrailing();
	}

}

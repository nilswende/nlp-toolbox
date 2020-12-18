package de.fernuni_hagen.kn.nlp.preprocessing.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.AbbreviationFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.TaggedWord;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Removes abbreviations from a sentence.
 *
 * @author Nils Wende
 */
public class FileAbbreviationFilter implements AbbreviationFilter {

	private final Set<String> abbreviations = new HashSet<>();

	/**
	 * Constructor.
	 *
	 * @param locale the sentence's language
	 */
	public FileAbbreviationFilter(final Locale locale) {
		final var inputStream = Objects.requireNonNull(FileAbbreviationFilter.class.getClassLoader().getResourceAsStream(mapLocale(locale)));
		try (final var lineIterator = IOUtils.lineIterator(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			while (lineIterator.hasNext()) {
				abbreviations.add(lineIterator.next());
			}
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private String mapLocale(final Locale locale) {
		switch (locale.getLanguage()) {
			case "de":
				return "abbreviations/abbrev.txt";
			default:
				throw new IllegalArgumentException("Unsupported locale: " + locale);
		}
	}

	@Override
	public Stream<TaggedWord> apply(final Stream<TaggedWord> sentence) {
		return sentence.filter(w -> !abbreviations.contains(w.getTerm()));
	}

}

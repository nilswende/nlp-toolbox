package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.AbbreviationRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.TaggedTerm;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Removes abbreviations from a sentence.
 *
 * @author Nils Wende
 */
public class FileAbbreviationRemover implements AbbreviationRemover {

	private final Set<String> abbreviations = new HashSet<>();

	/**
	 * Constructor.
	 *
	 * @param fileName the file containing the filtered abbreviations
	 */
	public FileAbbreviationRemover(final String fileName) {
		final var inputStream = Objects.requireNonNull(FileAbbreviationRemover.class.getClassLoader().getResourceAsStream(fileName));
		try (final var lineIterator = IOUtils.lineIterator(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			while (lineIterator.hasNext()) {
				abbreviations.add(lineIterator.next());
			}
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	@Override
	public Stream<TaggedTerm> apply(final Stream<TaggedTerm> sentence) {
		return sentence.filter(w -> !abbreviations.contains(w.getTerm()));
	}

}

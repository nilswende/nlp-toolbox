package de.fernuni_hagen.kn.nlp.preprocessing.textual;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.SentenceExtractor;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Wraps another SentenceExtractor and saves its sentences.
 *
 * @author Nils Wende
 */
public class SavingSentenceExtractor implements SentenceExtractor {

	private final SentenceExtractor extractor;
	private final PrintWriter writer;

	/**
	 * Creates a SavingSentenceExtractor.
	 *
	 * @param extractor another SentenceExtractor
	 * @param writer    target writer
	 */
	public SavingSentenceExtractor(final SentenceExtractor extractor, final PrintWriter writer) {
		this.extractor = extractor;
		this.writer = writer;
	}

	@Override
	public Stream<String> extract(final Path textFile) {
		final var sentences = extractor.extract(textFile);
		return sentences.peek(this::print).onClose(writer::close);
	}

	private void print(String sentence) {
		writer.println(sentence);
		writer.flush();
	}

}

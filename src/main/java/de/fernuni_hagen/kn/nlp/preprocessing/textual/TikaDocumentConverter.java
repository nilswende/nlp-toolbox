package de.fernuni_hagen.kn.nlp.preprocessing.textual;

import de.fernuni_hagen.kn.nlp.DocumentConverter;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts documents from any format to plain text using Apache Tika.
 *
 * @author Nils Wende
 */
public class TikaDocumentConverter implements DocumentConverter {

	private final int sentenceFileSizeLimitBytes;
	public static final Path DIR = Path.of("data", "sentencefiles");

	public TikaDocumentConverter(final int sentenceFileSizeLimitBytes) {
		this.sentenceFileSizeLimitBytes = sentenceFileSizeLimitBytes;
	}

	@Override
	public Path convert(final Reader reader, final String name) {
		final Path tempFile = getTempFile(name);
		try {
			Files.createDirectories(DIR);
			try (final var writer = Files.newBufferedWriter(tempFile, AppConfig.DEFAULT_CHARSET)) {
				parseInput(reader, writer);
				return tempFile;
			}
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private Path getTempFile(final String name) {
		return Path.of(DIR.resolve(name).toString() + ".txt");
	}

	private void parseInput(final Reader reader, final Writer writer) {
		try {
			final var stream = new ReaderInputStream(reader, AppConfig.DEFAULT_CHARSET);
			final var handler = new BodyContentHandler(new WriteOutContentHandler(writer, sentenceFileSizeLimitBytes));
			final var metadata = new Metadata();
			new AutoDetectParser().parse(stream, handler, metadata);
		} catch (final Exception e) {
			handleException(e);
		}
	}

	private void handleException(final Exception e) {
		// WriteLimitReachedException is private
		if ("WriteLimitReachedException".equals(e.getClass().getSimpleName())) {
			System.out.println(e.getMessage());
		} else {
			throw new UncheckedException(e);
		}
	}

}

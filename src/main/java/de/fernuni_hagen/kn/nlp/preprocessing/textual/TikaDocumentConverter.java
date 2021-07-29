package de.fernuni_hagen.kn.nlp.preprocessing.textual;

import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.preprocessing.DocumentConverter;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Converts documents from any format to plain text using Apache Tika.
 *
 * @author Nils Wende
 */
public class TikaDocumentConverter implements DocumentConverter {

	private final int sentenceFileSizeLimitBytes;
	private final boolean continueAfterReachingFileSizeLimit;

	public TikaDocumentConverter(final int sentenceFileSizeLimitBytes, final boolean continueAfterReachingFileSizeLimit) {
		this.sentenceFileSizeLimitBytes = sentenceFileSizeLimitBytes;
		this.continueAfterReachingFileSizeLimit = continueAfterReachingFileSizeLimit;
	}

	@Override
	public Path convert(final InputStream input, final String name) {
		final Path tempFile = FileHelper.getTempFile(name);
		try {
			try (final var writer = FileHelper.newBufferedWriter(tempFile)) {
				parseInput(input, writer, name);
				return tempFile;
			}
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private void parseInput(final InputStream input, final Writer writer, final String name) {
		final var writeOutContentHandler = new WriteOutContentHandler(writer, sentenceFileSizeLimitBytes);
		final var handler = new BodyContentHandler(writeOutContentHandler);
		final var metadata = new Metadata();
		try {
			new AutoDetectParser().parse(input, handler, metadata);
		} catch (final Exception e) {
			handleException(e, writeOutContentHandler, name);
		}
	}

	private void handleException(final Exception e, final WriteOutContentHandler handler, final String name) {
		if (handler.isWriteLimitReached(e)) {
			final var message = String.format("size limit of %s reached for file '%s'", sentenceFileSizeLimitBytes, name);
			if (!continueAfterReachingFileSizeLimit) {
				throw new UncheckedException(message, e);
			}
			System.out.println(message + ", continuing with the text up to the limit");
		} else {
			throw new UncheckedException(e);
		}
	}

}

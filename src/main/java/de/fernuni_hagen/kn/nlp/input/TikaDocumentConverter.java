package de.fernuni_hagen.kn.nlp.input;

import de.fernuni_hagen.kn.nlp.DocumentConverter;
import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts documents from any format to plain text using Apache Tika.
 *
 * @author Nils Wende
 */
public class TikaDocumentConverter implements DocumentConverter {

	private final Config config;

	public TikaDocumentConverter(final Config config) {
		this.config = config;
	}

	@Override
	public Path convert(final Path path) {
		final var tempFile = FileHelper.createTempFile(".txt");
		try (final var writer = Files.newBufferedWriter(tempFile, Config.DEFAULT_CHARSET)) {
			parseInput(path, writer);
			return tempFile;
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private void parseInput(final Path path, final Writer writer) {
		try (final var inputStream = Files.newInputStream(path)) {
			final var parser = new AutoDetectParser();
			final var contentHandler = new BodyContentHandler(new WriteOutContentHandler(writer, config.getSentenceFileSizeLimitBytes()));
			final var metadata = new Metadata();
			parser.parse(inputStream, contentHandler, metadata);
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

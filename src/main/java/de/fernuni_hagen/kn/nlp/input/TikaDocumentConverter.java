package de.fernuni_hagen.kn.nlp.input;

import de.fernuni_hagen.kn.nlp.DocumentConverter;
import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

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
	public File convert(final File file) {
		try {
			final var tempFile = FileHelper.createTempFile(".txt");
			try (final var writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
				parseInput(file, writer);
				return tempFile;
			}
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private void parseInput(final File file, final OutputStreamWriter writer) throws IOException {
		final var parser = new AutoDetectParser();
		final var contentHandler = new BodyContentHandler(writer);
		final var metadata = new Metadata();
		try (final var inputStream = new FileInputStream(file)) {
			parser.parse(inputStream, contentHandler, metadata);
		} catch (final TikaException | SAXException e) {
			throw new UncheckedException(e);
		}
	}

}

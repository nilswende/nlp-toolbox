package de.fernuni_hagen.kn.nlp.input;

import de.fernuni_hagen.kn.nlp.DocumentConverter;
import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
		final var tempFile = FileHelper.createTempFile(".txt");
		try (final var writer = new OutputStreamWriter(new FileOutputStream(tempFile), Config.DEFAULT_CHARSET)) {
			parseInput(file, writer);
			return tempFile;
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private void parseInput(final File file, final OutputStreamWriter writer) throws IOException {
		try (final var inputStream = new FileInputStream(file)) {
			final var parser = new AutoDetectParser();
			final var contentHandler = new BodyContentHandler(new WriteOutContentHandler(writer, config.getSentenceFileSizeLimitBytes()));
			final var metadata = new Metadata();
			parser.parse(inputStream, contentHandler, metadata);
		} catch (final TikaException e) {
			throw new UncheckedException(e);
		} catch (final SAXException e) {
			handleSAXException(e);
		}
	}

	private void handleSAXException(final SAXException e) {
		// WriteLimitReachedException is private
		if ("WriteLimitReachedException".equals(e.getClass().getSimpleName())) {
			System.out.println(e.getMessage());
		} else {
			throw new UncheckedException(e);
		}
	}

}

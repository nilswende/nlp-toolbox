package de.fernuni_hagen.kn.nlp.preprocessing.textual;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Nils Wende
 */
class TikaDocumentConverterTest {

	@Test
	void convert() {
		final var input = "big text";
		final var converter = new TikaDocumentConverter(input.length() - 1, false);
		assertThrows(UncheckedException.class, () -> converter.convert(new ReaderInputStream(new StringReader(input), AppConfig.DEFAULT_CHARSET), "file"));
	}

	@Test
	void convertContinue() {
		final var input = "big text";
		final var converter = new TikaDocumentConverter(input.length() - 1, true);
		assertDoesNotThrow(() -> converter.convert(new ReaderInputStream(new StringReader(input), AppConfig.DEFAULT_CHARSET), "file"));
	}

}

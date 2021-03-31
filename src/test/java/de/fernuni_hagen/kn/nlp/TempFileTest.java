package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Nils Wende
 */
public abstract class TempFileTest {

	protected static Path tempFile;

	@BeforeAll
	static void setUp() throws IOException {
		tempFile = Files.createTempFile("nlp", ".test");
		tempFile.toFile().deleteOnExit();
	}

	protected void writeString(final String input) throws IOException {
		Files.writeString(tempFile, input, AppConfig.DEFAULT_CHARSET);
	}

}

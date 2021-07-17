package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import org.junit.jupiter.api.AfterAll;
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
		final var tempDirectory = Files.createTempDirectory("nlp");
		tempFile = Files.createTempFile(tempDirectory, "nlp", ".test");
	}

	protected void writeString(final String input) throws IOException {
		Files.writeString(tempFile, input, AppConfig.DEFAULT_CHARSET);
	}

	@AfterAll
	static void afterAll() {
		FileHelper.deleteFile(tempFile);
	}

}

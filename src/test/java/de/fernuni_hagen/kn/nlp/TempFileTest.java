package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Nils Wende
 */
public abstract class TempFileTest {

	protected static Path tempDirectory;
	protected static Path tempFile;

	@BeforeAll
	static void setUpTempFileTest() throws IOException {
		tempDirectory = Files.createTempDirectory("nlp");
		tempFile = Files.createTempFile(tempDirectory, "nlp", ".test");
	}

	protected void writeString(final String input) throws IOException {
		Files.writeString(tempFile, input, AppConfig.DEFAULT_CHARSET);
	}

	@AfterAll
	static void afterAllTempFileTest() throws IOException {
		PathUtils.deleteDirectory(tempDirectory);
	}

}

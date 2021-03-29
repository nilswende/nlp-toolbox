package de.fernuni_hagen.kn.nlp;

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

}

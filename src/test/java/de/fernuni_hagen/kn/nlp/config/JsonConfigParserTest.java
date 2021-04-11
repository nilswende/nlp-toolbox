package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.TempFileTest;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Nils Wende
 */
class JsonConfigParserTest extends TempFileTest {

	@BeforeAll
	static void setUp() throws IOException {
		tempFile = Files.createTempFile("nlp", ".json");
		tempFile.toFile().deleteOnExit();
	}

	@Test
	void getAppConfig() {
		final var parser = new JsonConfigParser(new String[]{
				"-u", "hits"
		});
		Assertions.assertNotNull(parser.getAppConfig());
	}

	@Test
	void getAppConfigJson() {
		final var parser = new JsonConfigParser(new String[]{
				"-a", "{persistInMemoryDb:true}",
				"-u", "hits"
		});
		Assertions.assertNotNull(parser.getAppConfig());
	}

	@Test
	void throwIfNonExistingAppFile() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> new JsonConfigParser(new String[]{
						"-a", "\\nonExistingDir\\nonExistingFile.json",
						"-u", "hits"
				}));
	}

	@Test
	void throwIfInvalidAppFormat() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> new JsonConfigParser(new String[]{
						"-a", "persistInMemoryDb:true",
						"-u", "hits"
				}));
	}

	@Test
	void getUseCaseValues() {
		final var parser = new JsonConfigParser(new String[]{
				"-u", "hits", "-u", "pageRank",
				"-u", "{name:pagerank}",
				"-u", "{name:pagerank,", "weightingFunction:POISSON}"
		});
		Assertions.assertNotNull(parser.getUseCases());
	}

	@Test
	void getUseCaseValuesFromJsonArray() throws IOException {
		var json = "[\n{\n\"name\": \"pagerank\",\n\"resultLimit\": 25\n},\n{\n\"name\": \"hits\",\n\"resultLimit\": 25\n}\n]";
		writeString(json);
		final var parser = new JsonConfigParser(new String[]{"-u", tempFile.toString()});
		Assertions.assertNotNull(parser.getUseCases());
	}

	@Test
	void throwIfNonExistingFile() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> new JsonConfigParser(new String[]{
						"-u", "\\nonExistingDir\\nonExistingFile.json"
				}));
	}

	@Test
	void throwIfNoUseCases() {
		Assertions.assertThrows(UncheckedException.class,
				() -> new JsonConfigParser(new String[]{}));
	}

	@Test
	void throwIfNonExistingUseCase() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> new JsonConfigParser(new String[]{
						"-u", "nonExistingUseCase"
				}));
	}

	@Test
	void throwIfNonExistingUseCaseName() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> new JsonConfigParser(new String[]{
						"-u", "{namee:existingUseCase}"
				}));
	}
}

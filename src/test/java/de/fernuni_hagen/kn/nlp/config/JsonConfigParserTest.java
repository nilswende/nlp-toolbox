package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.TempFileTest;
import de.fernuni_hagen.kn.nlp.config.parser.JsonConfigParser;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

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
		assertNotNull(parser.getAppConfig());
	}

	@Test
	void getAppConfigJson() {
		final var parser = new JsonConfigParser(new String[]{
				"-a", "{persistInMemoryDb:true}",
				"-u", "hits"
		});
		final var appConfig = parser.getAppConfig();
		assertNotNull(appConfig);
		assertTrue(appConfig.persistInMemoryDb());
	}

	@Test
	void throwIfNonExistingAppFile() {
		assertThrows(IllegalArgumentException.class,
				() -> new JsonConfigParser(new String[]{
						"-a", "\\nonExistingDir\\nonExistingFile.json",
						"-u", "hits"
				}));
	}

	@Test
	void throwIfInvalidAppFormat() {
		assertThrows(IllegalArgumentException.class,
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
		final var useCases = parser.getUseCases();
		assertNotNull(useCases);
		assertEquals(4, useCases.size());
	}

	@Test
	void getUseCaseValuesFromJsonArray() throws IOException {
		final var json = "[\n{\n\"name\": \"pagerank\",\n\"resultLimit\": 25\n},\n{\n\"name\": \"hits\",\n\"resultLimit\": 25\n}\n]";
		writeString(json);
		final var parser = new JsonConfigParser(new String[]{"-u", tempFile.toString()});
		final var useCases = parser.getUseCases();
		assertNotNull(useCases);
		assertEquals(2, useCases.size());
	}

	@Test
	void throwIfNonExistingFile() {
		assertThrows(IllegalArgumentException.class,
				() -> new JsonConfigParser(new String[]{
						"-u", "\\nonExistingDir\\nonExistingFile.json"
				}));
	}

	@Test
	void throwIfNoUseCases() {
		assertThrows(UncheckedException.class,
				() -> new JsonConfigParser(new String[]{}));
	}

	@Test
	void throwIfNonExistingUseCase() {
		assertThrows(IllegalArgumentException.class,
				() -> new JsonConfigParser(new String[]{
						"-u", "nonExistingUseCase"
				}));
	}

	@Test
	void throwIfNonExistingUseCaseName() {
		assertThrows(IllegalArgumentException.class,
				() -> new JsonConfigParser(new String[]{
						"-u", "{namee:existingUseCase}"
				}));
	}
}

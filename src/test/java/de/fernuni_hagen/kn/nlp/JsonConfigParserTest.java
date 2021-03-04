package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.config.JsonConfigParser;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Nils Wende
 */
class JsonConfigParserTest {

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
		Assertions.assertNotNull(parser.getUseCaseConfigs());
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
						"-u", "{namee:nonExistingUseCase}"
				}));
	}
}

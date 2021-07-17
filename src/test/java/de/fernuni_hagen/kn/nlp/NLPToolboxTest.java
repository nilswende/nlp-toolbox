package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.analysis.BooleanRetrieval;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.ClearDatabase;
import de.fernuni_hagen.kn.nlp.preprocessing.Preprocessor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nils Wende
 */
class NLPToolboxTest {

	@Test
	void asLibrary() {
		final var appConfig = new AppConfig().setWorkingDir("").setDbDir("test");
		final var clearDatabase = new ClearDatabase();
		final var preprocessor = new Preprocessor(
				"text of words with multiple words",
				"1");
		final var booleanRetrieval = new BooleanRetrieval()
				.setType(BooleanRetrieval.Type.OR)
				.setQuery(List.of("words"));
		assertFalse(booleanRetrieval.hasResult());
		new NLPToolbox(appConfig).run(clearDatabase, preprocessor, booleanRetrieval);
		assertTrue(booleanRetrieval.hasResult());
		assertEquals(Map.of("1", 1L), booleanRetrieval.getResult().getDocuments());
	}

	@Test
	void asApplication() {
		assertDoesNotThrow(() -> NLPToolbox.main(new String[]{"-u", "hits"}));
	}

	@Test
	void others() {
		assertThrows(IllegalArgumentException.class, () -> new NLPToolbox().run());
	}

}

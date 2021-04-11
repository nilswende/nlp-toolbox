package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.analysis.BooleanRetrieval;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.preprocessing.Preprocessor;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nils Wende
 */
class NLPToolboxTest {

	@Test
	void asLibrary() {
		final var appConfig = new AppConfig().setBaseDir("").setDbDir("test");
		final var preprocessor = new Preprocessor()
				.setInput(new ReaderInputStream(new StringReader("text of words with multiple words"), AppConfig.DEFAULT_CHARSET))
				.setDocumentName("1");
		final var booleanRetrieval = new BooleanRetrieval()
				.setType(BooleanRetrieval.Type.OR)
				.setQuery(List.of("words"));
		new NLPToolbox(appConfig, preprocessor, booleanRetrieval).run();
		assertEquals(Map.of("1", 1L), booleanRetrieval.getResult().getDocuments());
	}

}

package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.analysis.BooleanRetrieval;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nils Wende
 */
class NLPToolboxTest {

	@Test
	void asLibrary() {
		final var booleanRetrieval = new BooleanRetrieval()
				.setType(BooleanRetrieval.Type.AND)
				.setQuery(List.of("abc"));
		new NLPToolbox(new AppConfig(), booleanRetrieval).run();
		assertTrue(booleanRetrieval.getResult().getDocuments().isEmpty());
	}

}

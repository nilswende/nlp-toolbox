package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.DBWriter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nils Wende
 */
class PreprocessorTest {

	@Test
	void execute() {
		final var preprocessor = new Preprocessor("Deutscher Text aus Wörtern mit mehreren Wörtern", "1")
				.setRemoveAbbreviations(true)
				.setDetectPhrases(true)
				.setUseBaseFormReduction(true)
				.setRemoveStopWords(true)
				.setNormalizeCase(true)
				.setKeepTempFiles(false)
				.setSaveSentenceFile(false)
				.setSentenceFileSizeLimitBytes(Integer.MAX_VALUE)
				.setContinueAfterReachingFileSizeLimit(false);
		execute(preprocessor);
	}

	private void execute(final Preprocessor preprocessor) {
		final var dbWriter = Mockito.mock(DBWriter.class);
		preprocessor.execute(dbWriter);
		assertTrue(preprocessor.hasResult());
	}

	@Test
	void executeDetectPhrases() {
		final var preprocessor = new Preprocessor("text of words with multiple words", "1")
				.setDetectPhrases(true)
				.setFilterNouns(true);
		execute(preprocessor);
		final var result = preprocessor.getResult();
		assertFalse(result.getPhrases().isEmpty());
	}

	@Test
	void executeExtractPhrases() {
		final var preprocessor = new Preprocessor("text of words with multiple words", "1")
				.setExtractPhrases(true);
		execute(preprocessor);
	}

	@Test
	void executeRemovePhrases() {
		final var preprocessor = new Preprocessor("text of words with multiple words", "1")
				.setRemovePhrases(true);
		execute(preprocessor);
	}

}

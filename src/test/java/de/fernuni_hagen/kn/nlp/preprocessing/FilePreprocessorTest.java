package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.TempFileTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nils Wende
 */
class FilePreprocessorTest extends TempFileTest {

	@Test
	void execute() throws IOException {
		writeString("text of words with multiple words");
		final var dbWriter = Mockito.mock(DBWriter.class);

		final var preprocessor = new FilePreprocessor();
		preprocessor.setInputDir(tempFile.getParent().toString())
				.setRemoveAbbreviations(true)
				.setExtractPhrases(true)
				.setUseBaseFormReduction(true)
				.setFilterNouns(true)
				.setRemoveStopWords(true)
				.setNormalizeCase(true);
		preprocessor.execute(dbWriter);
		assertTrue(preprocessor.hasResult());
		final var result = preprocessor.getResult();
		assertFalse(result.getDocumentNames().isEmpty());
		assertFalse(result.toString().isEmpty());
	}
}

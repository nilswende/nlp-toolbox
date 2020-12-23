package de.fernuni_hagen.kn.nlp.preprocessing;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Extracts the language from a text file.
 *
 * @author Nils Wende
 */
public interface LanguageExtractor {

	/**
	 * Extracts the language from a text file.
	 *
	 * @param textFile text file
	 * @return a Locale representing the text's language
	 */
	Locale extract(Path textFile);

}

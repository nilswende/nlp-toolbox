package de.fernuni_hagen.kn.nlp.input;

import java.io.File;
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
	Locale extract(File textFile);

}

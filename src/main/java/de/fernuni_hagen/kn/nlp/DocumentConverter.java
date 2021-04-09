package de.fernuni_hagen.kn.nlp;

import java.io.Reader;
import java.nio.file.Path;

/**
 * Converts documents from any format to plain text.
 *
 * @author Nils Wende
 */
@FunctionalInterface
public interface DocumentConverter {

	/**
	 * Converts the document to a plain text file.
	 *
	 * @param reader document in any format
	 * @param name   document name
	 * @return text file
	 */
	Path convert(Reader reader, String name);

}

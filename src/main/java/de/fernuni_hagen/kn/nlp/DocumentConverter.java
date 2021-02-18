package de.fernuni_hagen.kn.nlp;

import java.nio.file.Path;

/**
 * Converts documents from any format to plain text.
 *
 * @author Nils Wende
 */
@FunctionalInterface
public interface DocumentConverter {

	/**
	 * Converts the file to a plain text file.
	 *
	 * @param path file in any format
	 * @return text file
	 */
	Path convert(Path path);

}

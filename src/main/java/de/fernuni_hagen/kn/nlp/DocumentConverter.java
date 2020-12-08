package de.fernuni_hagen.kn.nlp;

import java.io.File;

/**
 * Converts documents from any format to plain text.
 *
 * @author Nils Wende
 */
public interface DocumentConverter {

	/**
	 * Converts the file to a plain text file.
	 *
	 * @param file file in any format
	 * @return text file
	 */
	File convert(File file);

}

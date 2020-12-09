package de.fernuni_hagen.kn.nlp;

import java.io.File;
import java.util.stream.Stream;

/**
 * Extracts sentences from a text file.
 *
 * @author Nils Wende
 */
@FunctionalInterface
public interface SentenceExtractor {

	/**
	 * Extracts sentences from a text file.
	 *
	 * @param textFile text file
	 * @return sentences
	 */
	Stream<String> extract(File textFile);

}

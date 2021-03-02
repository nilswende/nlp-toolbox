package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Extracts sentences from a text file.
 *
 * @author Nils Wende
 */
public interface SentenceExtractor {

	/**
	 * Extracts sentences from a text file.
	 *
	 * @param textFile text file
	 * @return sentences
	 */
	Stream<String> extract(Path textFile);

}

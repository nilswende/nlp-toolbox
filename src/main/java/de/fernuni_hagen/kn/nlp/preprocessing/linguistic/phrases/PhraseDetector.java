package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.phrases;

import java.util.List;

/**
 * Detects phrases in a text.
 *
 * @author Nils Wende
 */
public interface PhraseDetector {

	/**
	 * Detects phrases in a text.
	 *
	 * @param sentences the text in which phrases should be detected
	 * @return the list of distinct phrases in the text
	 */
	List<String> detectPhrases(List<String> sentences);

}

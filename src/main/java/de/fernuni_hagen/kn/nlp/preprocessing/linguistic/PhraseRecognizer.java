package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.List;

/**
 * Recognizes phrases in a text.
 *
 * @author Nils Wende
 */
public interface PhraseRecognizer {

	/**
	 * Recognizes phrases in a text.
	 *
	 * @param sentences the text in which phrases should be recognized
	 * @return the list of distinct phrases in the text
	 */
	List<String> recognizePhrases(List<String> sentences);

}

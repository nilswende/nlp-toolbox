package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Stream;

/**
 * Recognizes phrases in a text.
 *
 * @author Nils Wende
 */
public interface PhraseRecognizer {

	/**
	 * Recognizes phrases in a text.
	 * The phrases will be added to a separate list, but not removed from the original sentences.
	 *
	 * @param sentences the text in which phrases should be recognized
	 * @return the sentences and all phrases in the text
	 */
	Pair<Stream<String>, List<String>> recognizePhrases(Stream<String> sentences);

}

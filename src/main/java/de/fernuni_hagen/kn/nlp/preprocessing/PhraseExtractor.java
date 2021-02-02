package de.fernuni_hagen.kn.nlp.preprocessing;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Stream;

/**
 * Extracts phrases from a text.
 *
 * @author Nils Wende
 */
public interface PhraseExtractor {

	/**
	 * Extracts phrases from a text.
	 * The phrases are removed from the text's sentences and put in a different list.
	 *
	 * @param sentences the text from which phrases should be extracted
	 * @return the list of (terms, phrases) tuples, each forming one sentence
	 */
	Stream<Pair<String, List<String>>> extractPhrases(Stream<String> sentences);

}

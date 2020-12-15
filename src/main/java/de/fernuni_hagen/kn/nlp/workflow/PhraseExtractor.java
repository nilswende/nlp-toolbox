package de.fernuni_hagen.kn.nlp.workflow;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Locale;

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
	 * @param locale    the sentences language
	 * @param sentences the text from which phrases should be extracted
	 * @return the list of (sentence, phrases) tuples
	 */
	List<Pair<String, List<String>>> extractPhrases(Locale locale, List<String> sentences);

}

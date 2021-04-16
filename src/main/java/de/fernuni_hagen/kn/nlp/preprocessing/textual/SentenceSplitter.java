package de.fernuni_hagen.kn.nlp.preprocessing.textual;

import java.util.List;

/**
 * Splits a CharSequence into sentences.
 *
 * @author Nils Wende
 */
public interface SentenceSplitter {

	/**
	 * Splits a CharSequence into sentences.
	 *
	 * @param chars the CharSequence
	 * @return sentences
	 */
	List<String> split(CharSequence chars);

}

package de.fernuni_hagen.kn.nlp.preprocessing.textual;

/**
 * Removes excess whitespace from a CharSequence.
 *
 * @author Nils Wende
 */
public interface WhitespaceRemover {

	/**
	 * Removes excess whitespace from a CharSequence.
	 *
	 * @param chars the CharSequence
	 * @return a String containing at most one consecutive whitespace character
	 */
	String removeWhitespace(CharSequence chars);

}

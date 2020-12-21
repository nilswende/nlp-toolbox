package de.fernuni_hagen.kn.nlp.preprocessing;

/**
 * Removes unwanted characters from a CharSequence.
 *
 * @author Nils Wende
 */
public interface CharacterRemover {

	/**
	 * Removes unwanted characters from a CharSequence.
	 *
	 * @param chars the CharSequence
	 * @return a String containing no unwanted characters
	 */
	String removeCharacters(CharSequence chars);

}

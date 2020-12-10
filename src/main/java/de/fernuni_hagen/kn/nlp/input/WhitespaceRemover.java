package de.fernuni_hagen.kn.nlp.input;

/**
 * Removes excess whitespace from a CharSequence.
 *
 * @author Nils Wende
 */
public interface WhitespaceRemover {

	String removeWhitespace(CharSequence chars);

}

package de.fernuni_hagen.kn.nlp.utils;

/**
 * Unchecked Exception.
 *
 * @author Nils Wende
 */
public class UncheckedException extends RuntimeException {
	private static final long serialVersionUID = 2993451458567939813L;

	public UncheckedException(final Throwable cause) {
		super(cause);
	}

}

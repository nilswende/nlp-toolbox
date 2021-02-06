package de.fernuni_hagen.kn.nlp.utils;

/**
 * This class is typically used to wrap checked exceptions,
 * because there is usually no sensible way for the application to recover from one.
 *
 * @author Nils Wende
 */
public class UncheckedException extends RuntimeException {
	private static final long serialVersionUID = -1L;

	/**
	 * @see RuntimeException#RuntimeException(Throwable)
	 */
	public UncheckedException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @see RuntimeException#RuntimeException(String, Throwable)
	 */
	public UncheckedException(final String message, final Throwable cause) {
		super(message, cause);
	}
}

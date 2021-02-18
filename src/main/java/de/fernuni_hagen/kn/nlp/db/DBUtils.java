package de.fernuni_hagen.kn.nlp.db;

import java.nio.file.Path;

/**
 * Common DB utilities.
 *
 * @author Nils Wende
 */
public final class DBUtils {

	private DBUtils() {
		throw new AssertionError("no init");
	}

	/**
	 * Formats a Path to a normalized String.
	 *
	 * @param path Path
	 * @return String
	 */
	public static String normalizePath(final Path path) {
		return path.getFileName().toString();
	}

}

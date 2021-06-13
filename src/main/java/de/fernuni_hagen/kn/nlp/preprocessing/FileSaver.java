package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.nio.file.Path;

/**
 * Saves text to file.
 *
 * @author Nils Wende
 */
public class FileSaver {

	private final PrintWriter printWriter;

	/**
	 * Creates an instance from a file path.<br>
	 * If path is just a file name, a temp file will be created for it, else the path will be used as is.
	 *
	 * @param pathString file path
	 * @param print      true if text should be printed
	 */
	public FileSaver(final String pathString, final boolean print) {
		this(getPath(pathString), print);
	}

	private static Path getPath(final String pathString) {
		final var path = Path.of(pathString);
		return path.getNameCount() == 1 ? FileHelper.getTempFile(pathString) : path;
	}

	/**
	 * Creates an instance from a file path.
	 *
	 * @param path  file path
	 * @param print true if text should be printed
	 */
	public FileSaver(final Path path, final boolean print) {
		PrintWriter pw = null;
		if (print) {
			try {
				pw = FileHelper.newPrintWriter(path);
			} catch (final UncheckedException e) {
				System.err.println(e.getMessage());
			}
		}
		printWriter = pw == null ? new PrintWriter(NullOutputStream.NULL_OUTPUT_STREAM) : pw;
	}

	/**
	 * Prints an Object.
	 *
	 * @param o Object
	 */
	public void print(final Object o) {
		printWriter.print(o);
		printWriter.flush();
	}

	/**
	 * Prints an Object and then terminates the line.
	 *
	 * @param o Object
	 */
	public void println(final Object o) {
		printWriter.print(o);
		printWriter.print(StringUtils.LF);
		printWriter.flush();
	}

}

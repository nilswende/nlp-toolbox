package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.file.FileHelper;
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
	 * Creates an instance from a file name.
	 *
	 * @param name  file name
	 * @param print true if text should be printed
	 */
	public FileSaver(final String name, final boolean print) {
		this(FileHelper.getTempFile(name), print);
	}

	/**
	 * Creates an instance from a file path.
	 *
	 * @param path  file path
	 * @param print true if text should be printed
	 */
	public FileSaver(final Path path, final boolean print) {
		printWriter = print ? FileHelper.newPrintWriter(path) : new PrintWriter(NullOutputStream.NULL_OUTPUT_STREAM);
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

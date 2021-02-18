package de.fernuni_hagen.kn.nlp;

import java.nio.file.Path;

/**
 * A document.
 *
 * @author Nils Wende
 */
public class Document {

	private final Path originalFile;
	private final Path textFile;

	public Document(final Path originalFile, final Path textFile) {
		this.originalFile = originalFile;
		this.textFile = textFile;
	}

	public Path getOriginalFile() {
		return originalFile;
	}

	public Path getTextFile() {
		return textFile;
	}

}

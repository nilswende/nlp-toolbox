package de.fernuni_hagen.kn.nlp.file;

import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extracts all resources that need to be outside the JAR file.
 *
 * @author Nils Wende
 */
public final class ExternalResourcesExtractor {

	private ExternalResourcesExtractor() {
		throw new AssertionError("no init");
	}

	/**
	 * Extracts all resources that need to be outside the JAR file.
	 */
	public static void extractExternalResources() {
		final var zipName = "external/nlp-toolbox.zip";
		final var resource = ExternalResourcesExtractor.class.getClassLoader().getResourceAsStream(zipName);
		if (resource == null) {
			System.out.println("no file found in " + zipName);
			return;
		}
		unzip(resource);
	}

	private static void unzip(final InputStream resource) {
		try (final ZipInputStream zip = new ZipInputStream(resource)) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					final var fileName = entry.getName();
					final var file = new File(fileName);
					if (!file.exists()) {
						IOUtils.copy(zip, new FileOutputStream(file));
					}
				}
			}
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

}

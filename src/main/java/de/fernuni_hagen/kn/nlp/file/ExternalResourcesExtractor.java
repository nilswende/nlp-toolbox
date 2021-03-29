package de.fernuni_hagen.kn.nlp.file;

import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
		} else {
			unzip(resource);
		}
	}

	private static void unzip(final InputStream resource) {
		try (final ZipInputStream zip = new ZipInputStream(resource)) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					final var fileName = entry.getName();
					final var path = Path.of(fileName);
					if (!Files.exists(path)) {
						IOUtils.copy(zip, Files.newOutputStream(path));
					}
				}
			}
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

}

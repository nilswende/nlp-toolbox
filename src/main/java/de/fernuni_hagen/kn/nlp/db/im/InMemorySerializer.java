package de.fernuni_hagen.kn.nlp.db.im;

import com.google.gson.Gson;
import de.fernuni_hagen.kn.nlp.db.im.InMemoryDB.Content;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

/**
 * Persists the in-memory database.
 *
 * @author Nils Wende
 */
class InMemorySerializer {

	/**
	 * Persists the in-memory database when the JVM shuts down.
	 *
	 * @param path    the target file
	 * @param content the in-memory database's state
	 * @throws UncheckedException if there's an IO exception while writing the file
	 */
	public static void persistOnShutdown(final Path path, final Content content) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> persist(path, content)));
	}

	/**
	 * Persists the in-memory database.
	 *
	 * @param path    the target file
	 * @param content the in-memory database's state
	 * @throws UncheckedException if there's an IO exception while writing the file
	 */
	public static void persist(final Path path, final Content content) {
		try (final var writer = new OutputStreamWriter(
				new GZIPOutputStream(FileHelper.newBufferedOutputStream(path)),
				StandardCharsets.UTF_8)) {
			new Gson().toJson(content, writer);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

}

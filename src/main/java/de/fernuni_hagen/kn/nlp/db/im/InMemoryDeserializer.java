package de.fernuni_hagen.kn.nlp.db.im;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.fernuni_hagen.kn.nlp.db.im.InMemoryDB.Content;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/**
 * Reads the in-memory database from disk.
 *
 * @author Nils Wende
 */
class InMemoryDeserializer {

	/**
	 * Deserializes the in-memory database from disk or returns a new one, if the file doesn't exist.
	 *
	 * @param path the persisted file
	 * @return the in-memory database's state
	 * @throws UncheckedException if there's an IO exception while reading the file
	 */
	public static Content deserialize(final Path path) {
		return Files.exists(path) ? deserializeJson(path) : Content.init();
	}

	private static Content deserializeJson(final Path path) {
		try (final var reader = new InputStreamReader(
				new GZIPInputStream(new BufferedInputStream(Files.newInputStream(path))),
				StandardCharsets.UTF_8)) {
			return new Gson().fromJson(reader, getType());
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private static Type getType() {
		return new TypeToken<Content>() {
		}.getType();
	}

}

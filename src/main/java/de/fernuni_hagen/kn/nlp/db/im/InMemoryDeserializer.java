package de.fernuni_hagen.kn.nlp.db.im;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

/**
 * Reads the in-memory database from disk.
 *
 * @author Nils Wende
 */
class InMemoryDeserializer {

	/**
	 * Deserializes the in-memory database from disk or returns a new Map, if the file doesn't exist.
	 *
	 * @param path the persisted file
	 * @return the in-memory database's state
	 * @throws UncheckedException if there's an IO exception while reading the file
	 */
	public static Map<String, InMemoryDB.Values> deserialize(final Path path) {
		return Files.exists(path) ? deserializeJson(path) : new TreeMap<>();
	}

	private static Map<String, InMemoryDB.Values> deserializeJson(final Path path) {
		try (final var reader = new InputStreamReader(
				new GZIPInputStream(Files.newInputStream(path), IOUtils.DEFAULT_BUFFER_SIZE),
				StandardCharsets.UTF_8)) {
			return new Gson().fromJson(reader, getType());
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private static Type getType() {
		return new TypeToken<Map<String, InMemoryDB.Values>>() {
		}.getType();
	}

}

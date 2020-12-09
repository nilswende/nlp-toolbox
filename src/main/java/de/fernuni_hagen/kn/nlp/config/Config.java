package de.fernuni_hagen.kn.nlp.config;

import com.google.gson.Gson;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * Config class for NLPToolbox.
 * It can be created from a JSON file in the default config location.
 *
 * @author Nils Wende
 */
public class Config {

	private static final String DEFAULT_BASE_DIR = "data";
	private static final String DEFAULT_CONFIG_DIR = "config";
	private static final String DEFAULT_CONFIG_FILENAME = "config.json";

	private String baseDir;
	private String inputDir;
	private String configDir;
	private String configFile;

	public String getBaseDir() {
		return defaultIfNull(baseDir, DEFAULT_BASE_DIR);
	}

	public Path getInputDir() {
		return Path.of(getBaseDir(), defaultIfNull(inputDir, "input"));
	}

	public Path getConfigDir() {
		return Path.of(defaultIfNull(inputDir, DEFAULT_CONFIG_DIR));
	}

	public static Path getDefaultConfigFilePath() {
		return Path.of(DEFAULT_CONFIG_DIR, DEFAULT_CONFIG_FILENAME);
	}

	/**
	 * Create the config from the config file.
	 *
	 * @param configFileName path to the config file
	 * @return Config
	 */
	public static Config fromJson(final String configFileName) {
		final var configFile = (configFileName == null ? getDefaultConfigFilePath() : Path.of(configFileName)).toFile();
		if (configFile.exists()) {
			return fromJson(configFile);
		} else {
			return fromJson(new StringReader("{}"));
		}
	}

	private static Config fromJson(final File configFile) {
		try (final var reader = newFileReader(configFile)) {
			return fromJson(reader);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private static Reader newFileReader(final File file) throws IOException {
		return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
	}

	private static Config fromJson(final Reader reader) {
		return new Gson().fromJson(reader, Config.class);
	}

}

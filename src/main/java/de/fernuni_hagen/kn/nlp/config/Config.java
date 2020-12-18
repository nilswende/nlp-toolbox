package de.fernuni_hagen.kn.nlp.config;

import com.google.gson.Gson;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
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

	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	private static final String EMPTY_JSON = "{}";
	private static final String DEFAULT_BASE_DIR = "data";
	private static final String DEFAULT_CONFIG_DIR = "config";
	private static final String DEFAULT_CONFIG_FILENAME = "config.json";

	private String baseDir;
	private String inputDir;
	private String dbDir;
	private int sentenceFileSizeLimitBytes;
	private boolean keepTempFiles;
	private boolean extractPhrases;
	private boolean useBaseFormReduction;
	private boolean filterNouns;
	private boolean removeStopWords;
	private boolean removeAbbreviations;

	public String getBaseDir() {
		return defaultIfNull(baseDir, DEFAULT_BASE_DIR);
	}

	public Path getInputDir() {
		return Path.of(getBaseDir(), defaultIfNull(inputDir, "input"));
	}

	public Path getDbDir() {
		return Path.of(getBaseDir(), defaultIfNull(dbDir, "db"));
	}

	public int getSentenceFileSizeLimitBytes() {
		return sentenceFileSizeLimitBytes <= 0 ? Integer.MAX_VALUE : sentenceFileSizeLimitBytes;
	}

	public boolean keepTempFiles() {
		return keepTempFiles;
	}

	public boolean extractPhrases() {
		return extractPhrases;
	}

	public boolean useBaseFormReduction() {
		return useBaseFormReduction;
	}

	public boolean filterNouns() {
		return filterNouns;
	}

	public boolean removeStopWords() {
		return removeStopWords;
	}

	public boolean removeAbbreviations() {
		return removeAbbreviations;
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
		return configFile.exists() ? fromJson(configFile) : fromJson(new StringReader(EMPTY_JSON));
	}

	private static Config fromJson(final File configFile) {
		try (final var reader = FileHelper.newFileReader(configFile)) {
			return fromJson(reader);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private static Config fromJson(final Reader reader) {
		return new Gson().fromJson(reader, Config.class);
	}

}

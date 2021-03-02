package de.fernuni_hagen.kn.nlp.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * Application-wide config class for NLPToolbox.
 *
 * @author Nils Wende
 */
public class AppConfig {

	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	public static final String DEFAULT_BASE_DIR = "data";
	private static final String DEFAULT_CONFIG_DIR = "config";
	private static final String DEFAULT_CONFIG_FILENAME = "config.json";
	public static final String DB_IN_MEMORY = "im";
	public static final String DB_NEO4J = "neo4j";

	/*
	 * It is important that this class and all other config classes contain no final fields,
	 * since GSON will not be able to correctly overwrite them.
	 * If a default value is needed, handle it in the getter.
	 */

	private String baseDir;
	private String inputDir;
	private String dbDir;
	private String db;
	private boolean analysisOnly;
	private boolean persistInMemoryDb;

	public String getBaseDir() {
		return defaultIfNull(baseDir, DEFAULT_BASE_DIR);
	}

	public Path getInputDir() {
		return Path.of(getBaseDir(), defaultIfNull(inputDir, "input"));
	}

	public Path getDbDir() {
		return Path.of(getBaseDir(), defaultIfNull(dbDir, "db"));
	}

	public Path getInMemoryDbDir() {
		return getDbDir().resolve(DB_IN_MEMORY);
	}

	public Path getNeo4JDbDir() {
		return getDbDir().resolve(DB_NEO4J);
	}

	public String getDb() {
		return db == null ? DB_IN_MEMORY : db;
	}

	public boolean analysisOnly() {
		return analysisOnly;
	}

	public boolean persistInMemoryDb() {
		return persistInMemoryDb;
	}

	public static Path getDefaultConfigFilePath() {
		return Path.of(DEFAULT_CONFIG_DIR, DEFAULT_CONFIG_FILENAME);
	}

}

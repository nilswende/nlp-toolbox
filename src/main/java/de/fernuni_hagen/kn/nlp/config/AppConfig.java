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
	private static final String DEFAULT_WORKING_DIR = "data";

	private String workingDir;
	private String dbDir;
	private DbType db;
	private boolean persistInMemoryDb;

	/**
	 * Database type.
	 */
	public enum DbType {
		IN_MEMORY("im"), NEO4J("neo4j");
		private final String dir;

		DbType(final String dir) {
			this.dir = dir;
		}

		public String getDir() {
			return dir;
		}
	}

	public String getWorkingDir() {
		return defaultIfNull(workingDir, DEFAULT_WORKING_DIR);
	}

	public Path getDbDir() {
		return Path.of(getWorkingDir(), defaultIfNull(dbDir, "db"));
	}

	public Path getInMemoryDbDir() {
		return getDbDir().resolve(getDb().getDir());
	}

	public Path getNeo4JDbDir() {
		return getDbDir().resolve(getDb().getDir());
	}

	public DbType getDb() {
		return db == null ? DbType.IN_MEMORY : db;
	}

	public boolean persistInMemoryDb() {
		return persistInMemoryDb;
	}

	/**
	 * Set the working directory.
	 *
	 * @param workingDir the working directory
	 * @return this object
	 */
	public AppConfig setWorkingDir(final String workingDir) {
		this.workingDir = workingDir;
		return this;
	}

	/**
	 * Set the database directory (relative to the working directory).
	 *
	 * @param dbDir the database directory
	 * @return this object
	 */
	public AppConfig setDbDir(final String dbDir) {
		this.dbDir = dbDir;
		return this;
	}

	/**
	 * Set the database.
	 *
	 * @param db the database type
	 * @return this object
	 */
	public AppConfig setDb(final DbType db) {
		this.db = db;
		return this;
	}

	/**
	 * Set true, if the in memory db should be persisted (if used at all), false otherwise.
	 *
	 * @param persistInMemoryDb true, if the in memory db should be persisted (if used at all)
	 * @return this object
	 */
	public AppConfig setPersistInMemoryDb(final boolean persistInMemoryDb) {
		this.persistInMemoryDb = persistInMemoryDb;
		return this;
	}
}

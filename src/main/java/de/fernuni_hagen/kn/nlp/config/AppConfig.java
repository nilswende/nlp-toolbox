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

	/**
	 * The default charset, which is used for all reads/writes on bytes controlled by this application.
	 */
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	private static final Path DEFAULT_WORKING_DIR = Path.of("data");

	/*
	 * It is important that this class and all other configurable classes contain no final fields,
	 * since GSON will not be able to correctly overwrite them.
	 * If a default value is needed, set the field or handle it in the getter.
	 */

	private Path workingDir;
	private Path dbDir;
	private DbType db;
	private boolean persistInMemoryDb;
	private double defaultSignificance = 0.01; // or Double.MIN_VALUE

	/**
	 * Database type.
	 */
	public enum DbType {
		IN_MEMORY("im"), NEO4J("neo4j");
		private final Path dir;

		DbType(final String dir) {
			this.dir = Path.of(dir);
		}

		public Path getDir() {
			return dir;
		}
	}

	public Path getWorkingDir() {
		return defaultIfNull(workingDir, DEFAULT_WORKING_DIR);
	}

	public Path getDbDir() {
		return getWorkingDir().resolve(defaultIfNull(dbDir, Path.of("db")));
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

	public double getDefaultSignificance() {
		return defaultSignificance;
	}

	/**
	 * Set the default significance used for cooccurrences that should not be considered for an analysis.
	 *
	 * @param defaultSignificance the default significance
	 * @return this object
	 */
	public AppConfig setDefaultSignificance(final double defaultSignificance) {
		this.defaultSignificance = defaultSignificance;
		return this;
	}

	/**
	 * Set the working directory.
	 *
	 * @param workingDir the working directory
	 * @return this object
	 */
	public AppConfig setWorkingDir(final String workingDir) {
		this.workingDir = Path.of(workingDir);
		return this;
	}

	/**
	 * Set the database directory (will be relative to the working directory).
	 *
	 * @param dbDir the database directory
	 * @return this object
	 */
	public AppConfig setDbDir(final String dbDir) {
		this.dbDir = Path.of(dbDir);
		return this;
	}

	/**
	 * Set the database type.
	 *
	 * @param db the database type
	 * @return this object
	 */
	public AppConfig setDb(final DbType db) {
		this.db = db;
		return this;
	}

	/**
	 * Set true, if the in-memory db should be persisted (if used at all), false otherwise.
	 *
	 * @param persistInMemoryDb true, if the in-memory db should be persisted (if used at all)
	 * @return this object
	 */
	public AppConfig setPersistInMemoryDb(final boolean persistInMemoryDb) {
		this.persistInMemoryDb = persistInMemoryDb;
		return this;
	}
}

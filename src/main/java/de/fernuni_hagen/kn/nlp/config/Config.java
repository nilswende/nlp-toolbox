package de.fernuni_hagen.kn.nlp.config;

import com.google.gson.Gson;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.math.DirectedWeightingFunctions;
import de.fernuni_hagen.kn.nlp.math.WeightingFunctions;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * Config class for NLPToolbox.<br>
 * It can be created from a JSON file in the default config location.<br>
 *
 * @author Nils Wende
 */
public class Config {

	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	private static final String DEFAULT_BASE_DIR = "data";
	private static final String DEFAULT_CONFIG_DIR = "config";
	private static final String DEFAULT_CONFIG_FILENAME = "config.json";
	public static final String DB_IN_MEMORY = "im";
	public static final String DB_NEO4J = "neo4j";

	/**
	 * It is important that this class and all inner classes contain no final fields,
	 * since GSON will not be able to correctly overwrite them.
	 */
	private String baseDir;
	private String inputDir;
	private String dbDir;
	private String db = DB_IN_MEMORY;
	private boolean analysisOnly;
	private boolean persistInMemoryDb;
	private boolean keepTempFiles;
	private int sentenceFileSizeLimitBytes;
	private boolean extractPhrases;
	private boolean useBaseFormReduction;
	private boolean filterNouns;
	private boolean removeStopWords;
	private boolean removeAbbreviations;
	private boolean normalizeCase;
	private AnalysisConfig analysis = new AnalysisConfig();

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
		return db;
	}

	public boolean analysisOnly() {
		return analysisOnly;
	}

	public boolean persistInMemoryDb() {
		return persistInMemoryDb;
	}

	public boolean keepTempFiles() {
		return keepTempFiles;
	}

	public int getSentenceFileSizeLimitBytes() {
		return sentenceFileSizeLimitBytes <= 0 ? Integer.MAX_VALUE : sentenceFileSizeLimitBytes;
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

	public boolean normalizeCase() {
		return normalizeCase;
	}

	public AnalysisConfig getAnalysisConfig() {
		return analysis;
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
		final var configFile = configFileName == null ? getDefaultConfigFilePath() : Path.of(configFileName);
		return Files.exists(configFile) ? fromJson(configFile) : new Config();
	}

	private static Config fromJson(final Path configFile) {
		try (final var reader = FileHelper.newFileReader(configFile)) {
			return fromJson(reader);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private static Config fromJson(final Reader reader) {
		return new Gson().fromJson(reader, Config.class);
	}

	/**
	 * Contains the analysis config.
	 */
	public static class AnalysisConfig {

		private PageRankConfig pageRank = new PageRankConfig();
		private HITSConfig hits = new HITSConfig();

		public PageRankConfig getPageRankConfig() {
			return pageRank;
		}

		public HITSConfig getHitsConfig() {
			return hits;
		}

		/**
		 * Contains the PageRank config.
		 */
		public static class PageRankConfig {
			private boolean calculate;
			private int iterations = 25;
			private int resultLimit = Integer.MAX_VALUE;
			private double weight = 0.85;
			private WeightingFunctions weightingFunction = WeightingFunctions.DICE;

			public boolean calculate() {
				return calculate;
			}

			public int getIterations() {
				return iterations;
			}

			public int getResultLimit() {
				return resultLimit;
			}

			public double getWeight() {
				return weight;
			}

			public WeightingFunctions getWeightingFunction() {
				return weightingFunction;
			}
		}

		/**
		 * Contains the HITS config.
		 */
		public static class HITSConfig {
			private boolean calculate;
			private int iterations = 50;
			private int resultLimit = Integer.MAX_VALUE;
			private DirectedWeightingFunctions directedWeightingFunction = DirectedWeightingFunctions.DIRECTED;

			public boolean calculate() {
				return calculate;
			}

			public int getIterations() {
				return iterations;
			}

			public int getResultLimit() {
				return resultLimit;
			}

			public DirectedWeightingFunctions getDirectedWeightingFunction() {
				return directedWeightingFunction;
			}
		}

	}

}

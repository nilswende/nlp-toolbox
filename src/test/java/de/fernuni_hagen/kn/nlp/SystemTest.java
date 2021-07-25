package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.analysis.HITS;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.ClearDatabase;
import de.fernuni_hagen.kn.nlp.preprocessing.Preprocessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * @author Nils Wende
 */
class SystemTest {

	@Test
	void asLibrary() throws IOException {
		final var doc = "a_1.txt";
		final var name = "systemtest/" + doc;
		final var expectedPR = getScoreMap(name + "_PageRank.txt");
		final var expectedAuths = getScoreMap(name + "_Auths.txt");
		final var expectedHubs = getScoreMap(name + "_Hubs.txt");

		try (final var input = getResourceAsStream(name)) {
			final var appConfig = new AppConfig().setWorkingDir("").setDbDir("test");
			final var clearDatabase = new ClearDatabase();
			final var preprocessor = new Preprocessor(input, doc)
					.setUseBaseFormReduction(true)
					.setFilterNouns(true)
					.setRemoveStopWords(true)
					.setNormalizeCase(true);
			final var pageRank = new PageRank();
			final var hits = new HITS();

			new NLPToolbox(appConfig).run(clearDatabase, preprocessor, pageRank, hits);

			pageRank.getResult().getScores().forEach((term, score) -> {
				final var expectedScore = expectedPR.get(term);
				Assertions.assertNotNull(expectedScore);
				Assertions.assertEquals(expectedScore, score, .00001, term);
			});
			hits.getResult().getAuthorityScores().forEach((term, score) -> {
				final var expectedScore = expectedAuths.get(term);
				Assertions.assertNotNull(expectedScore);
				Assertions.assertEquals(expectedScore, score, .00001, term);
			});
			hits.getResult().getHubScores().forEach((term, score) -> {
				final var expectedScore = expectedHubs.get(term);
				Assertions.assertNotNull(expectedScore);
				Assertions.assertEquals(expectedScore, score, .00001, term);
			});
		}
	}

	private Map<String, Double> getScoreMap(final String name) {
		final var map = new TreeMap<String, Double>();
		try (final var scanner = new Scanner(getResourceAsStream(name))) {
			while (scanner.hasNextLine()) {
				final var term = scanner.next();
				final var score = Double.parseDouble(scanner.next());
				map.put(term, score);
			}
		}
		return map;
	}

	private InputStream getResourceAsStream(final String name) {
		return Objects.requireNonNull(SystemTest.class.getClassLoader().getResourceAsStream(name));
	}

}

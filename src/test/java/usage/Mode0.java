package usage;

import de.fernuni_hagen.kn.nlp.NLPToolbox;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.analysis.HITS;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.ClearDatabase;
import de.fernuni_hagen.kn.nlp.preprocessing.FilePreprocessor;

/**
 * How the previous Mode 0 would be configured.
 * Note that there are no fixed modes for the new NLPToolbox!
 * Simply create a main method with the desired configuration and run it.
 * There is no need to run one mode and then the other.
 *
 * @author Nils Wende
 */
public class Mode0 {

	public static void main(final String[] args) {
		final long start = System.currentTimeMillis();
		// create the app config
		final var appConfig = new AppConfig();
		// create the use case steps
		final var clearDatabase = new ClearDatabase();
		final var preprocessor = new FilePreprocessor()
				.setKeepTempFiles(true)
				.setSaveSentenceFile(true)
				//.setRemoveAbbreviations(true)
				.setDetectPhrases(true)
				.setFilterNouns(true)
				.setNormalizeCase(true)
				.setUseBaseFormReduction(true)
				.setRemoveStopWords(true);
		final var pageRank = new PageRank();
		final var hits = new HITS();
		// run the NLPToolbox
		new NLPToolbox(appConfig).run(clearDatabase, preprocessor, pageRank, hits)
				// process the results
				.stream().map(UseCase::getResult).forEach(System.out::println);

		final long end = System.currentTimeMillis();
		System.out.println("Processing took " + (end - start) / 1000.0 + " seconds.");
	}

}

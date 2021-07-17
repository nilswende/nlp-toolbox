package usage;

import de.fernuni_hagen.kn.nlp.NLPToolbox;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.analysis.HITS;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.ClearDatabase;
import de.fernuni_hagen.kn.nlp.preprocessing.FilePreprocessor;

import java.util.List;

import static de.fernuni_hagen.kn.nlp.Logger.logCurrentThreadCpuTime;

/**
 * A full run of the NLPToolbox.
 * Read files, analyze the cooccurrence graph and print the results to the console.
 *
 * @author Nils Wende
 */
public class Full {

	public static void main(final String[] args) {
		// create the app config
		final var appConfig = new AppConfig();
		// create the use case steps
		final var clearDatabase = new ClearDatabase();
		final var preprocessor = new FilePreprocessor()
				.setKeepTempFiles(false)
				.setRemoveAbbreviations(true)
				.setExtractPhrases(true)
				.setUseBaseFormReduction(true)
				.setFilterNouns(true)
				.setRemoveStopWords(true)
				.setNormalizeCase(true);
		final var pageRank = new PageRank().setResultLimit(10);
		final var hits = new HITS().setResultLimit(10);
		final var useCases = List.of(clearDatabase, preprocessor, pageRank, hits);
		// run the NLPToolbox
		new NLPToolbox(appConfig).run(useCases);
		// process the results
		useCases.stream().map(UseCase::getResult).forEach(System.out::println);
		logCurrentThreadCpuTime();
	}

}

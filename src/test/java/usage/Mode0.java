package usage;

import de.fernuni_hagen.kn.nlp.NLPToolbox;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.analysis.HITS;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.ClearDatabase;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.preprocessing.FilePreprocessor;

import java.util.List;

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
		// create the app config
		final var appConfig = new AppConfig();
		// create the use case steps
		final var clearDatabase = new ClearDatabase();
		final var preprocessor = new FilePreprocessor()
				.setKeepTempFiles(true)
				.setSaveSentenceFile(true)
				//.setRemoveAbbreviations(true) // DE only
				.setDetectPhrases(true)
				.setFilterNouns(true)
				.setNormalizeCase(true)
				.setUseBaseFormReduction(true)
				.setRemoveStopWords(true);
		final var pageRank = new PageRank().setWeightingFunction(WeightingFunction.ASSN);
		final var hits = new HITS().setWeightingFunction(WeightingFunction.ASSN);
		final var useCases = List.of(clearDatabase, preprocessor, pageRank, hits);
		// run the NLPToolbox
		new NLPToolbox(appConfig, useCases).run();
		// process the results
		useCases.stream().map(UseCase::getResult).forEach(System.out::println);
	}

}

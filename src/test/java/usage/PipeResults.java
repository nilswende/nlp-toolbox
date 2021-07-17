package usage;

import de.fernuni_hagen.kn.nlp.NLPToolbox;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.analysis.CentroidBySpreadingActivation;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.ClearDatabase;
import de.fernuni_hagen.kn.nlp.preprocessing.FilePreprocessor;

import java.util.List;

/**
 * This example shows how to use a result of one NLPToolbox invocation as input for another.
 * Note that the state of the database needs to be persisted between the two invocations
 * and that the NLPToolbox object can be reused.
 *
 * @author Nils Wende
 */
public class PipeResults {

	public static void main(final String[] args) {
		// create the app config
		final var appConfig = new AppConfig().setPersistInMemoryDb(true);
		final var nlpToolbox = new NLPToolbox(appConfig);
		// create the use case steps
		final var clearDatabase = new ClearDatabase();
		final var preprocessor = new FilePreprocessor()
				.setUseBaseFormReduction(true)
				.setFilterNouns(true)
				.setRemoveStopWords(true)
				.setNormalizeCase(true);
		final var pageRank = new PageRank().setResultLimit(5);
		final var useCases = List.of(clearDatabase, preprocessor, pageRank);
		// run the NLPToolbox
		nlpToolbox.run(useCases);
		// process the results
		useCases.stream().map(UseCase::getResult).forEach(System.out::println);
		final var topPageRanked = List.copyOf(pageRank.getResult().getScores().keySet());
		// create another use case step
		final var centroidBySpreadingActivation = new CentroidBySpreadingActivation().setQuery(topPageRanked);
		// run the NLPToolbox
		nlpToolbox.run(centroidBySpreadingActivation);
		// process the results
		System.out.println(centroidBySpreadingActivation.getResult());
	}

}

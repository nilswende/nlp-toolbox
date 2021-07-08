package usage;

import de.fernuni_hagen.kn.nlp.NLPToolbox;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.analysis.HITS;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.ClearDatabase;
import de.fernuni_hagen.kn.nlp.preprocessing.FilePreprocessor;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.util.List;
import java.util.stream.Collectors;

import static de.fernuni_hagen.kn.nlp.Logger.logCurrentThreadCpuTime;

/**
 * How to recover from a failed execution.
 * Find the non-executed use cases and retry executing them.
 *
 * @author Nils Wende
 */
public class Recover {

	public static void main(final String[] args) {
		// create the app config
		final var appConfig = new AppConfig().setPersistInMemoryDb(true);
		// create the use case steps
		final var clearDatabase = new ClearDatabase();
		final var preprocessor = new FilePreprocessor();
		final var pageRank = new PageRank().setResultLimit(10);
		final var hits = new HITS().setResultLimit(10);
		final var useCases = List.of(clearDatabase, preprocessor, pageRank, hits);
		try {
			// run the NLPToolbox
			new NLPToolbox(appConfig, useCases).run();
			// process the results
			useCases.stream().map(UseCase::getResult).forEach(System.out::println);
		} catch (final UncheckedException e) {
			// retry
			final var notExecuted = useCases.stream().filter(useCase -> !useCase.hasResult()).collect(Collectors.toList());
			new NLPToolbox(appConfig, notExecuted).run();
			// process the results
			notExecuted.stream().map(UseCase::getResult).forEach(System.out::println);
		}
		logCurrentThreadCpuTime();
	}

}

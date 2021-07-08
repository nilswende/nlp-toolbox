package usage;

import de.fernuni_hagen.kn.nlp.NLPToolbox;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.ClearDatabase;
import de.fernuni_hagen.kn.nlp.preprocessing.FilePreprocessor;

import java.time.Duration;
import java.util.List;

import static de.fernuni_hagen.kn.nlp.Logger.logCurrentThreadCpuTime;

/**
 * Persist the preprocessed terms in a database.
 *
 * @author Nils Wende
 * @see Analyze
 */
public class Persist {

	public static void main(final String[] args) {
		// create the app config
		final var appConfig = new AppConfig().setPersistInMemoryDb(true);
		// create the use case steps
		final var clearDatabase = new ClearDatabase();
		final var preprocessor = new FilePreprocessor()
				.setKeepTempFiles(false)
				.setExtractPhrases(false)
				.setUseBaseFormReduction(true)
				.setFilterNouns(true)
				.setRemoveStopWords(true)
				.setNormalizeCase(true);
		final var useCases = List.of(clearDatabase, preprocessor);
		// run the NLPToolbox
		final var start = System.nanoTime();
		new NLPToolbox(appConfig, useCases).run();
		final var duration = Duration.ofNanos(System.nanoTime() - start);
		// process the results
		useCases.stream().map(UseCase::getResult).forEach(System.out::println);
		System.out.println(String.format("total duration: %d s %d ms", duration.toSecondsPart(), duration.toMillisPart()));
		logCurrentThreadCpuTime();
	}

}

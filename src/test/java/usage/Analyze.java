package usage;

import de.fernuni_hagen.kn.nlp.NLPToolbox;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.analysis.HITS;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.config.AppConfig;

import java.time.Duration;
import java.util.List;

import static de.fernuni_hagen.kn.nlp.utils.Logger.logCurrentThreadCpuTime;

/**
 * Use a persisted database to analyze its content.
 *
 * @author Nils Wende
 * @see Persist
 */
public class Analyze {

	public static void main(final String[] args) {
		// create the app config
		final var appConfig = new AppConfig();
		// create the use case steps
		final var pageRank = new PageRank().setResultLimit(10);
		final var hits = new HITS().setResultLimit(10);
		final var useCases = List.of(pageRank, hits);
		// run the NLPToolbox
		final var start = System.nanoTime();
		new NLPToolbox(appConfig).run(useCases);
		final var duration = Duration.ofNanos(System.nanoTime() - start);
		// process the results
		useCases.stream().map(UseCase::getResult).forEach(System.out::println);
		System.out.println(String.format("total duration: %d s %d ms", duration.toSecondsPart(), duration.toMillisPart()));
		logCurrentThreadCpuTime();
	}

}

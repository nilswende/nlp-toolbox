package usage;

import de.fernuni_hagen.kn.nlp.NLPToolbox;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.file.Exporter;
import de.fernuni_hagen.kn.nlp.utils.ResultPrinter;

/**
 * Export use case results to a file.
 *
 * @author Nils Wende
 */
public class Export {

	public static void main(final String[] args) {
		// create the use case
		final var pageRank = new PageRank().setResultLimit(10);
		// run the NLPToolbox
		new NLPToolbox().run(pageRank);
		// export the results
		final var printer = new ResultPrinter().printMap(pageRank.getResult().getScores());
		new Exporter("data/output/PageRank.txt").print(printer);
	}

}

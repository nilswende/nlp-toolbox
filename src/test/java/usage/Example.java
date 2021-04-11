package usage;

import de.fernuni_hagen.kn.nlp.NLPToolbox;
import de.fernuni_hagen.kn.nlp.analysis.PageRank;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.preprocessing.Preprocessor;
import org.apache.commons.lang3.StringUtils;

/**
 * An example of how to use the NLPToolbox.
 *
 * @author Nils Wende
 */
public class Example {

	/**
	 * An example of how to use the NLPToolbox.
	 *
	 * @param args the document text
	 */
	public static void main(final String[] args) {
		final var docText = String.join(StringUtils.SPACE, args);
		// create the app config
		final var appConfig = new AppConfig().setWorkingDir("").setDbDir("any");
		// create the use case steps
		final var preprocessor = new Preprocessor(docText, "any");
		final var pageRank = new PageRank().setResultLimit(5);
		// run the NLPToolbox
		new NLPToolbox(appConfig, preprocessor, pageRank).run();
		// process the results
		System.out.println(pageRank.getResult());
	}

}

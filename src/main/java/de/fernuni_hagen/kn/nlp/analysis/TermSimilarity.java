package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.config.UseCaseConfig;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import org.apache.commons.collections4.SetUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calculates the similarity of given terms.
 *
 * @author Nils Wende
 */
public class TermSimilarity extends UseCase {

	private final Config config;

	public TermSimilarity(final Config config) {
		this.config = config;
	}

	@Override
	public void execute(final DBReader dbReader) {
		final var sim = calculate(dbReader);
		print(sim);
	}

	/**
	 * TermSimilarity config.
	 */
	public static class Config extends UseCaseConfig {
		private int compareFirstN;
		private WeightingFunction weightingFunction;
		private String term1;
		private String term2;

		public int getCompareFirstN() {
			return compareFirstN;
		}

		public WeightingFunction getWeightingFunction() {
			return weightingFunction == null ? WeightingFunction.DICE : weightingFunction;
		}

		public String getTerm1() {
			return term1;
		}

		public String getTerm2() {
			return term2;
		}
	}

	/**
	 * Calculates the similarity of given terms.
	 *
	 * @param db DB
	 * @return the term similarity
	 */
	public double calculate(final DBReader db) {
		final var function = config.getWeightingFunction();
		final var significances = db.getSignificances(function);
		final var cooccTerms1 = getMostSignificantCooccs(config.getTerm1(), significances);
		final var cooccTerms2 = getMostSignificantCooccs(config.getTerm2(), significances);
		final var commonTerms = SetUtils.intersection(cooccTerms1, cooccTerms2).size();
		return commonTerms == 0 ? 0 : function.calculate(cooccTerms1.size(), cooccTerms2.size(), commonTerms, 1, 1);
	}

	private Set<String> getMostSignificantCooccs(final String term, final Map<String, Map<String, Double>> significances) {
		return significances.getOrDefault(term, Map.of()).entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.limit(config.getCompareFirstN())
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}

}

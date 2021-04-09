package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;
import org.apache.commons.collections4.SetUtils;

import java.util.Map;
import java.util.Set;

/**
 * Calculates the similarity of given terms.
 *
 * @author Nils Wende
 */
public class TermSimilarity extends UseCase {

	private int compareFirstN;
	private WeightingFunction weightingFunction = WeightingFunction.DICE;
	private String term1;
	private String term2;

	private Result result;

	/**
	 * Term similarity result.
	 */
	public static class Result extends UseCase.Result {
		private final String term1;
		private final String term2;
		private final double similarity;

		public Result(final String term1, final String term2, final double similarity) {
			this.term1 = term1;
			this.term2 = term2;
			this.similarity = similarity;
		}

		@Override
		protected void printResult() {
			printf("Similarity between '%s' and '%s': %s", term1, term2, similarity);
		}

		public double getSimilarity() {
			return similarity;
		}
	}

	@Override
	public void execute(final DBReader dbReader) {
		final var significances = dbReader.getSignificances(weightingFunction);
		final var cooccTerms1 = getMostSignificantCooccs(term1, significances);
		final var cooccTerms2 = getMostSignificantCooccs(term2, significances);
		final var commonTerms = SetUtils.intersection(cooccTerms1, cooccTerms2).size();
		final var similarity = commonTerms == 0 ? 0
				: weightingFunction.calculate(cooccTerms1.size(), cooccTerms2.size(), commonTerms, 1, 1);
		result = new Result(term1, term2, similarity);
	}

	private Set<String> getMostSignificantCooccs(final String term, final Map<String, Map<String, Double>> significances) {
		return Maps.topN(significances.getOrDefault(term, Map.of()), compareFirstN).keySet();
	}

	@Override
	public Result getResult() {
		return result;
	}

	/**
	 * Set the number of terms with highest significance to compare for each document.
	 *
	 * @param compareFirstN the number of terms
	 * @return this object
	 */
	public TermSimilarity setCompareFirstN(final int compareFirstN) {
		this.compareFirstN = compareFirstN;
		return this;
	}

	/**
	 * Set the function to calculate the weight of each term.
	 *
	 * @param weightingFunction the weighting function
	 * @return this object
	 */
	public TermSimilarity setWeightingFunction(final WeightingFunction weightingFunction) {
		this.weightingFunction = weightingFunction;
		return this;
	}

	/**
	 * Set the first term to compare.
	 *
	 * @param term1 the first term
	 * @return this object
	 */
	public TermSimilarity setTerm1(final String term1) {
		this.term1 = term1;
		return this;
	}

	/**
	 * Set the second term to compare.
	 *
	 * @param term2 the second term
	 * @return this object
	 */
	public TermSimilarity setTerm2(final String term2) {
		this.term2 = term2;
		return this;
	}
}

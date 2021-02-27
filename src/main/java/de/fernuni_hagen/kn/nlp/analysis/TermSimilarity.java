package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
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
public class TermSimilarity {

	String term1, term2;
	WeightingFunction function;
	int compareFirstN;

	/**
	 * Calculates the similarity of given terms.
	 *
	 * @param db DB
	 * @return the term similarity
	 */
	public double calculate(final DBReader db) {
		final var significances = db.getSignificances(function);
		final var cooccTerms1 = getMostSignificantCooccs(term1, significances);
		final var cooccTerms2 = getMostSignificantCooccs(term2, significances);
		final var commonTerms = SetUtils.intersection(cooccTerms1, cooccTerms2).size();
		return commonTerms == 0 ? 0 : function.calculate(cooccTerms1.size(), cooccTerms2.size(), commonTerms, 1, 1);
	}

	private Set<String> getMostSignificantCooccs(final String term, final Map<String, Map<String, Double>> significances) {
		return significances.getOrDefault(term, Map.of()).entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.limit(compareFirstN)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}

}

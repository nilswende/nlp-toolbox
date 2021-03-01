package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.TermSimConfig;
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

	private final TermSimConfig config;

	public TermSimilarity(final TermSimConfig config) {
		this.config = config;
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

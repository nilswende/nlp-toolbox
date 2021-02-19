package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.math.DirectedWeightingFunction;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.util.Map;

/**
 * Reads from the database.
 *
 * @author Nils Wende
 */
public interface DBReader {

	/**
	 * Gets all term cooccurrences.
	 *
	 * @return a mapping from each term to each cooccurring term with their respective number of cooccurrences
	 */
	Map<String, Map<String, Double>> getCooccurrences();

	/**
	 * Gets the significance coefficient of all term cooccurrences via the weighting function.
	 *
	 * @param function the weighting function
	 * @return a mapping from each term to each cooccurring term with their respective significance coefficient
	 */
	Map<String, Map<String, Double>> getSignificances(WeightingFunction function);

	/**
	 * Gets the significance coefficient of all term cooccurrences via the directed weighting function.
	 *
	 * @param function the directed weighting function
	 * @return a mapping from each term to each cooccurring term with their respective significance coefficient
	 */
	Map<String, Map<String, Double>> getSignificances(DirectedWeightingFunction function);

	/**
	 * Gets all terms frequencies.
	 *
	 * @return a mapping from each term and document it is contained in to their respective count
	 */
	MultiKeyMap<String, Double> getTermFrequencies();
}

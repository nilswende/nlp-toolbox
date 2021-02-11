package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.math.DirectedWeightingFunctions;
import de.fernuni_hagen.kn.nlp.math.WeightingFunctions;

import java.util.List;
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
	 * @return a mapping from each term to each cooccurring term
	 */
	Map<String, List<String>> getCooccurrences();

	/**
	 * Gets the significance coefficient of all term cooccurrences via the weighting function.
	 *
	 * @param function the weighting function
	 * @return a mapping from each term to each cooccurring term with their respective significance coefficient
	 */
	Map<String, Map<String, Double>> getSignificances(WeightingFunctions function);

	/**
	 * Gets the significance coefficient of all term cooccurrences via the directed weighting function.
	 *
	 * @param function the directed weighting function
	 * @return a mapping from each term to each cooccurring term with their respective significance coefficient
	 */
	Map<String, Map<String, Double>> getSignificances(DirectedWeightingFunctions function);

}

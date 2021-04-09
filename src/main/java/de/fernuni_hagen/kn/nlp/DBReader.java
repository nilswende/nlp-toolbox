package de.fernuni_hagen.kn.nlp;

import de.fernuni_hagen.kn.nlp.graph.WeightedPath;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;

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
	 * Gets the significance coefficient of all term cooccurrences via the weighting function.<br>
	 * The direction of each term association is decided by the higher term occurrence.
	 * It points from the term with the higher occurrence to the term with lower occurrence.
	 *
	 * @param function the weighting function
	 * @return a mapping from each term to each cooccurring term with their respective significance coefficient
	 */
	Map<String, Map<String, Double>> getDirectedSignificances(WeightingFunction function);

	/**
	 * Gets all terms frequencies.
	 *
	 * @return a mapping from each term to each document it is contained in with their respective count
	 */
	Map<String, Map<String, Long>> getTermFrequencies();

	/**
	 * Gets the shortest path between the two nodes.
	 *
	 * @param start    start node
	 * @param end      end node
	 * @param function the weighting function
	 * @return a weighted path
	 */
	WeightedPath getShortestPath(String start, String end, WeightingFunction function);

	/**
	 * Gets all sentences in the given document.
	 *
	 * @param name original name of a preprocessed document
	 * @return all sentences in the given document
	 */
	List<List<String>> getAllSentencesInDocument(String name);
}

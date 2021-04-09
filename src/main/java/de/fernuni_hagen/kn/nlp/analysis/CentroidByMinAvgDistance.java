package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.graph.DijkstraSearcher;
import de.fernuni_hagen.kn.nlp.graph.WeightedPath;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.List;
import java.util.Map;

/**
 * Finds a centroid by calculating the minimum average distance.
 *
 * @author Nils Wende
 */
public class CentroidByMinAvgDistance extends UseCase {

	private WeightingFunction weightingFunction = WeightingFunction.DICE;
	private List<String> query;

	private Result result;

	public class Result extends UseCase.Result {
		private final String centroid;

		Result(final String centroid) {
			this.centroid = centroid;
			print(centroid);
		}

		public String getCentroid() {
			return centroid;
		}
	}

	@Override
	public void execute(final DBReader dbReader) {
		final var centroid = calculate(dbReader);
		result = new Result(centroid);
	}

	/**
	 * Finds the centroid of the given query set (most frequent terms).
	 *
	 * @param db DB
	 * @return the centroid or null, if the query is too diverse
	 */
	public String calculate(final DBReader db) {
		final List<String> query = this.query;
		if (query == null || query.size() <= 1) {
			return null;
		}
		final var significances = db.getSignificances(weightingFunction);

		final var distances = Maps.invertValues(significances);
		return findCentroid(query, distances);
	}

	private String findCentroid(final List<String> query, final Map<String, Map<String, Double>> distances) {
		String centroid = null;
		double minAvgDistance = Double.MAX_VALUE;
		final var dijkstra = new DijkstraSearcher();
		for (final String term : distances.keySet()) {
			if (!query.contains(term)) {
				final var avgDistance = query.stream()
						.map(q -> dijkstra.search(term, q, distances))
						.filter(WeightedPath::exists)
						.mapToDouble(WeightedPath::getWeight)
						.average()
						.orElse(Double.MAX_VALUE);
				if (avgDistance < minAvgDistance) {
					centroid = term;
					minAvgDistance = avgDistance;
				}
			}
		}
		return centroid;
	}

	@Override
	public Result getResult() {
		return result;
	}

	/**
	 * Set the weighting function used to calculate the distance between nodes.
	 *
	 * @param weightingFunction the weighting function
	 * @return this object
	 */
	public CentroidByMinAvgDistance setWeightingFunction(final WeightingFunction weightingFunction) {
		this.weightingFunction = weightingFunction;
		return this;
	}

	/**
	 * Set the terms used to calculate the centroid.
	 *
	 * @param query the terms used to calculate the centroid
	 * @return this object
	 */
	public CentroidByMinAvgDistance setQuery(final List<String> query) {
		this.query = query;
		return this;
	}

}

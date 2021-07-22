package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.graph.DijkstraSearcher;
import de.fernuni_hagen.kn.nlp.graph.WeightedPath;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;
import de.fernuni_hagen.kn.nlp.utils.ResultPrinter;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Checks if the highest path weight between any two query terms is between the given minimum (inclusive) and maximum (exclusive).
 *
 * @author Nils Wende
 */
public class TestQuery extends UseCase {

	private List<String> query;
	private double minPathWeight;
	private double maxPathWeight;
	private WeightingFunction weightingFunction = WeightingFunction.DICE;

	private transient Result result;

	/**
	 * TestQuery result.
	 */
	public static class Result extends UseCase.Result {
		private final boolean success;

		Result(final boolean success) {
			this.success = success;
		}

		@Override
		public void toString(final ResultPrinter printer) {
			printer.print(success);
		}

		/**
		 * Returns true, if the highest path weight between any two query terms is between the given minimum (inclusive) and maximum (exclusive), false otherwise.
		 *
		 * @return true, if the highest path weight between any two query terms is between the given minimum (inclusive) and maximum (exclusive)
		 */
		public boolean isSuccess() {
			return success;
		}
	}

	@Override
	public void execute(final DBReader dbReader) {
		var success = false;
		if (query != null
				&& query.size() > 1
				&& query.stream().allMatch(Objects::nonNull)
				&& Set.copyOf(query).size() == query.size()
				&& dbReader.containsTerms(query)) {
			final var distances = Maps.invertValues(dbReader.getSignificances(weightingFunction));
			final var paths = new DijkstraSearcher().search(query, distances);
			final var anyPaths = paths.values().stream()
					.filter(p -> p.size() == query.size())
					.findAny(); // any list of paths is sufficient, since having the same size as the query means that the paths connect every query term
			if (anyPaths.isPresent()) {
				final double maxDistance = anyPaths.get().stream()
						.max(Comparator.comparingDouble(WeightedPath::getWeight))
						.map(WeightedPath::getWeight)
						.orElseThrow();
				success = minPathWeight <= maxDistance && maxDistance < maxPathWeight;
			}
		}
		result = new Result(success);
	}

	@Override
	public Result getResult() {
		return result;
	}

	/**
	 * Set the terms to be tested.
	 *
	 * @param query the terms to be tested
	 * @return this object
	 */
	public TestQuery setQuery(final List<String> query) {
		this.query = query;
		return this;
	}

	/**
	 * Set the minimum path weight.
	 *
	 * @param minPathWeight the minimum path weight
	 * @return this object
	 */
	public TestQuery setMinPathWeight(final double minPathWeight) {
		this.minPathWeight = minPathWeight;
		return this;
	}

	/**
	 * Set the maximum path weight.
	 *
	 * @param maxPathWeight the maximum path weight
	 * @return this object
	 */
	public TestQuery setMaxPathWeight(final double maxPathWeight) {
		this.maxPathWeight = maxPathWeight;
		return this;
	}

	/**
	 * Set the weighting function used to calculate the distance between nodes.
	 *
	 * @param weightingFunction the weighting function
	 * @return this object
	 */
	public TestQuery setWeightingFunction(final WeightingFunction weightingFunction) {
		this.weightingFunction = weightingFunction;
		return this;
	}
}

package de.fernuni_hagen.kn.nlp.graph;

import java.util.List;

/**
 * Contains the shortest path from the start node to the end node and the total weight of the path.
 */
public class WeightedPath {

	private final List<String> path;
	private final double weight;

	/**
	 * Creates a new empty path with a total weight of {@link Double#POSITIVE_INFINITY}.
	 */
	public WeightedPath() {
		this(List.of(), Double.POSITIVE_INFINITY);
	}

	/**
	 * Creates a new path.
	 *
	 * @param path   the path ordered from start to end
	 * @param weight the total weight of the path
	 */
	public WeightedPath(final List<String> path, final double weight) {
		this.path = List.copyOf(path);
		this.weight = weight;
	}

	public List<String> getPath() {
		return path;
	}

	public double getWeight() {
		return weight;
	}

}

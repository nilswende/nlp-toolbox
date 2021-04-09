package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;

/**
 * A use case.
 *
 * @author Nils Wende
 */
public abstract class UseCase {

	private final StringWriter stringWriter = new StringWriter();
	private final PrintWriter printWriter = new PrintWriter(stringWriter);

	/**
	 * Executes the use case by handing over a DBReader and a DBWriter.
	 *
	 * @param dbReader DBReader
	 * @param dbWriter DBWriter
	 */
	public void execute(final DBReader dbReader, final DBWriter dbWriter) {
		final var start = System.nanoTime();
		execute(dbReader);
		execute(dbWriter);
		getResult().setDuration(start, System.nanoTime());
	}

	/**
	 * Hook method to be used by use cases that only need a DBReader.
	 *
	 * @param dbReader DBReader
	 */
	protected void execute(final DBReader dbReader) {

	}

	/**
	 * Hook method to be used by use cases that only need a DBWriter.
	 *
	 * @param dbWriter DBWriter
	 */
	protected void execute(final DBWriter dbWriter) {

	}

	private String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Prints the object.
	 *
	 * @param o Object
	 */
	protected void print(final Object o) {
		printWriter.println(o);
	}

	/**
	 * Prints the format.
	 *
	 * @param format format string
	 * @param args   arguments
	 */
	protected void printf(final String format, final Object... args) {
		printWriter.printf(format, args);
		printWriter.println();
	}

	/**
	 * Prints the concrete use case's name and the collection.
	 *
	 * @param collection   the collection
	 * @param emptyMessage message if the collection is empty
	 * @param format       message for each collection entry
	 */
	protected void printfCollection(final Collection<?> collection, final String emptyMessage, final String format) {
		if (collection.isEmpty()) {
			print(emptyMessage);
		} else {
			collection.forEach(e -> printf(format, e));
		}
	}

	/**
	 * Prints the concrete use case's name and the map.
	 *
	 * @param map          the map
	 * @param emptyMessage message if the map is empty
	 * @param format       message for each map entry
	 */
	protected void printfMap(final Map<?, ?> map, final String emptyMessage, final String format) {
		if (map.isEmpty()) {
			print(emptyMessage);
		} else {
			map.forEach((k, v) -> printf(format, k, v));
		}
	}

	/**
	 * Prints the concrete use case's name and the map.
	 *
	 * @param map          the map
	 * @param emptyMessage message if the map is empty
	 * @param format       message for each inner map entry
	 * @param <K>          inner key type
	 * @param <V>          value type
	 */
	protected <K, V> void printfMapMap(final Map<?, Map<K, V>> map, final String emptyMessage, final String format) {
		if (map.isEmpty()) {
			print(emptyMessage);
		} else {
			map.forEach((k1, m) -> m.forEach((k2, v) -> printf(format, k1, k2, v)));
		}
	}

	/**
	 * Limit the scores to n entries.
	 *
	 * @param scores Map
	 * @param limit  limit
	 * @return limited Map
	 */
	protected static Map<String, Double> topNScores(final Map<String, Double> scores, final int limit) {
		return scores.entrySet().stream()
				.sorted(comparingByValue(Comparator.reverseOrder()))
				.limit(limit)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Returns the use case's result.
	 *
	 * @return the use case's result
	 */
	// must be overridden in every subclass to return the concrete result type
	public abstract Result getResult();

	/**
	 * The basic use case result.
	 * Will usually be overridden by a use case implementation to return a more specific result.
	 */
	public class Result {
		private Duration duration;

		@Override
		public String toString() {
			printWriter.println("start " + getName());
			printWriter.println("  end " + getName());
			printWriter.println(String.format("duration: %d s %d ms", duration.toSecondsPart(), duration.toMillisPart()));
			printWriter.flush();
			return stringWriter.toString();
		}

		void setDuration(final long start, final long end) {
			this.duration = Duration.ofNanos(end - start);
		}
	}

}

package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;

/**
 * A use case of the NLPToolbox.
 *
 * @author Nils Wende
 */
public abstract class UseCase {

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

	/**
	 * Returns this use case's result.
	 *
	 * @return this use case's result
	 */
	// must be overridden in every subclass to return the concrete result type
	public abstract Result getResult();

	/**
	 * The basic use case result.<br>
	 * Must be implemented by every use case implementation to be able to return a more specific result (including the use case's name).
	 */
	public static abstract class Result {
		private Duration duration;
		private PrintWriter printWriter;

		/**
		 * Returns a string representation of this result's content.
		 *
		 * @return a string representation of this result's content
		 */
		@Override
		public String toString() {
			final StringWriter stringWriter = new StringWriter();
			printWriter = new PrintWriter(stringWriter);
			printWriter.println("start " + getName());
			printResult();
			printWriter.println("end " + getName());
			printWriter.println(String.format("%s duration: %d s %d ms", getName(), duration.toSecondsPart(), duration.toMillisPart()));
			printWriter.flush();
			printWriter = null;
			return stringWriter.toString();
		}

		/**
		 * Hook method to print the specific result's contents.
		 */
		protected void printResult() {

		}

		private String getName() {
			return this.getClass().getDeclaringClass().getSimpleName();
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
		 * Returns the time spent executing this result's use case.
		 *
		 * @return the time spent
		 */
		public Duration getDuration() {
			return duration;
		}

		void setDuration(final long start, final long end) {
			this.duration = Duration.ofNanos(end - start);
		}
	}

}

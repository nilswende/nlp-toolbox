package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.util.Collection;
import java.util.Map;

import static de.fernuni_hagen.kn.nlp.Logger.logDuration;
import static de.fernuni_hagen.kn.nlp.Logger.logStart;

/**
 * A use case.
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
		final var start = logStart(getName());
		printName();
		execute(dbReader);
		execute(dbWriter);
		logDuration(getName(), start);
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
	 * Prints the concrete use case's name.
	 */
	private void printName() {
		System.out.print(getName());
		System.out.println(":");
	}

	/**
	 * Prints the object.
	 *
	 * @param o Object
	 */
	protected void print(final Object o) {
		System.out.println(o);
	}

	/**
	 * Prints the format.
	 *
	 * @param format format string
	 * @param args   arguments
	 */
	protected void printf(final String format, final Object... args) {
		System.out.printf(format, args);
		System.out.println();
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
	 */
	protected <K, V> void printfMapMap(final Map<?, Map<K, V>> map, final String emptyMessage, final String format) {
		if (map.isEmpty()) {
			print(emptyMessage);
		} else {
			map.forEach((k1, m) -> m.forEach((k2, v) -> printf(format, k1, k2, v)));
		}
	}

	/**
	 * Creates a new UseCase instance from its config.
	 * For this to work the config has to be declared inside the concrete UseCase class.
	 *
	 * @param configClass UseCaseConfig
	 * @return new UseCase instance
	 */
	public static UseCase from(final UseCaseConfig configClass) {
		try {
			final Class<?> useCaseClass = configClass.getClass().getDeclaringClass();
			return (UseCase) useCaseClass.getConstructor(configClass.getClass()).newInstance(configClass);
		} catch (final Exception e) {
			throw new UncheckedException(e);
		}
	}

}

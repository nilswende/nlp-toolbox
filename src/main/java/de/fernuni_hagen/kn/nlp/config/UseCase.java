package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

import java.util.Collection;
import java.util.Map;

/**
 * A use case.
 *
 * @author Nils Wende
 */
public abstract class UseCase {

	private static final String EMPTY_MESSAGE = "No data available";

	/**
	 * Executes the use case by handing over a DBReader and a DBWriter.
	 *
	 * @param dbReader DBReader
	 * @param dbWriter DBWriter
	 */
	public void execute(final DBReader dbReader, final DBWriter dbWriter) {
		execute(dbReader);
		execute(dbWriter);
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
	 * Prints the concrete use case's name.
	 */
	protected void printName() {
		System.out.println(this.getClass().getSimpleName() + ":");
	}

	/**
	 * Prints the concrete use case's name and the map.
	 *
	 * @param map Collection
	 */
	protected void printNameAnd(final Map<?, ?> map) {
		printNameAnd(map.isEmpty() ? EMPTY_MESSAGE : map);
	}

	/**
	 * Prints the concrete use case's name and the collection.
	 *
	 * @param collection Collection
	 */
	protected void printNameAnd(final Collection<?> collection) {
		printNameAnd(collection.isEmpty() ? EMPTY_MESSAGE : collection);
	}

	/**
	 * Prints the concrete use case's name and the object.
	 *
	 * @param o Object
	 */
	protected void printNameAnd(final Object o) {
		printName();
		print(o);
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
	 * Prints the concrete use case's name and the format.
	 *
	 * @param format format string
	 * @param args   arguments
	 */
	protected void printfNameAnd(final String format, final Object... args) {
		printName();
		printf(format, args);
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

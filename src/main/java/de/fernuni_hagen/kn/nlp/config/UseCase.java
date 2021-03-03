package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

/**
 * A use case.
 *
 * @author Nils Wende
 */
public interface UseCase {

	// hook methods

	/**
	 * Executes the use case by handing over a DBReader and a DBWriter.
	 *
	 * @param dbReader DBReader
	 * @param dbWriter DBWriter
	 */
	default void execute(final DBReader dbReader, final DBWriter dbWriter) {
		execute(dbReader);
		execute(dbWriter);
	}

	/**
	 * Hook method to be used by use cases that only need a DBReader.
	 *
	 * @param dbReader DBReader
	 */
	default void execute(final DBReader dbReader) {

	}

	/**
	 * Hook method to be used by use cases that only need a DBWriter.
	 *
	 * @param dbWriter DBWriter
	 */
	default void execute(final DBWriter dbWriter) {

	}

	// printing

	/**
	 * Prints the concrete use case's name.
	 */
	default void print() {
		System.out.println(this.getClass().getSimpleName() + ":");
	}

	/**
	 * Prints the concrete use case's name and the object.
	 *
	 * @param o Object
	 */
	default void print(final Object o) {
		print();
		System.out.println(o);
	}

	/**
	 * Creates a new UseCase instance from its config.
	 * For this to work the config has to be declared inside the concrete UseCase class.
	 *
	 * @param configClass UseCaseConfig
	 * @return new UseCase instance
	 */
	static UseCase from(final UseCaseConfig configClass) {
		try {
			final Class<?> useCaseClass = configClass.getClass().getDeclaringClass();
			return (UseCase) useCaseClass.getConstructor(configClass.getClass()).newInstance(configClass);
		} catch (final Exception e) {
			throw new UncheckedException(e);
		}
	}

}

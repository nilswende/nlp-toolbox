package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;

/**
 * @author Nils Wende
 */
public interface UseCase {

	void execute(DBReader dbReader, DBWriter dbWriter);

	default void print() {
		System.out.println(this.getClass().getSimpleName() + ":");
	}

	default void print(final Object o) {
		print();
		System.out.println(o);
	}

	static UseCase from(final UseCaseConfig configClass) {
		try {
			final Class<?> useCaseClass = configClass.getClass().getDeclaringClass();
			return (UseCase) useCaseClass.getConstructor(configClass.getClass()).newInstance(configClass);
		} catch (final Exception e) {
			throw new UncheckedException(e);
		}
	}

}

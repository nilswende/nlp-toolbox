package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.config.UseCaseConfig;

/**
 * Clears the database.
 *
 * @author Nils Wende
 */
public class ClearDatabase extends UseCase {

	public ClearDatabase(final Config config) {
	}

	/**
	 * ClearDatabase config.
	 */
	public static class Config extends UseCaseConfig {
	}

	@Override
	public void execute(final DBWriter dbWriter) {
		dbWriter.deleteAll();
	}

}

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

	@Override
	public void execute(DBWriter dbWriter) {
		dbWriter.deleteAll();
	}

	/**
	 * ClearDatabase config.
	 */
	public static class Config extends UseCaseConfig {
	}

}

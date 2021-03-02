package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.UseCaseConfig;

/**
 * Clears the database.
 *
 * @author Nils Wende
 */
public class ClearDatabase {

	/**
	 * Clears the database.
	 *
	 * @param db DB
	 */
	public void clearDatabase(final DBWriter db) {
		db.deleteAll();
	}

	/**
	 * ClearDatabase config.
	 */
	public static class Config extends UseCaseConfig {
	}

}

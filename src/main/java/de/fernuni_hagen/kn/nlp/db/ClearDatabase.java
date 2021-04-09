package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.config.UseCase;

/**
 * Clears the database.
 *
 * @author Nils Wende
 */
public class ClearDatabase extends UseCase {

	@Override
	public void execute(final DBWriter dbWriter) {
		dbWriter.deleteAll();
	}

	@Override
	public Result getResult() {
		return new Result();
	}

}

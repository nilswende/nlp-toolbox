package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.UseCase;

/**
 * Clears the database.
 *
 * @author Nils Wende
 */
public class ClearDatabase extends UseCase {

	private Result result;

	/**
	 * ClearDatabase result.
	 */
	public static class Result extends UseCase.Result {
	}

	@Override
	public void execute(final DBWriter dbWriter) {
		dbWriter.deleteAll();
		result = new Result();
	}

	@Override
	public Result getResult() {
		return result;
	}

}

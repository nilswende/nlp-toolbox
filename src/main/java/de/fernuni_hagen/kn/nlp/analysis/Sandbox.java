package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.NLPToolbox;
import de.fernuni_hagen.kn.nlp.UseCase;

/**
 * Sandbox for instantly testing new code.
 *
 * @author Nils Wende
 */
public class Sandbox extends UseCase {

	@Override
	protected void execute(final DBReader dbReader) {
	}

	public static void main(final String[] args) {
		new NLPToolbox().run(new Sandbox());
	}

	@Override
	public Result getResult() {
		return new Result() {
		};
	}
}

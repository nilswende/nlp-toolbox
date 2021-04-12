package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.output.NullPrintStream;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

/**
 * Prints Cypher statements to a PrintStream.
 *
 * @author Nils Wende
 */
public class StatementPrinter {

	/**
	 * Set true to print to a file.
	 */
	private static final boolean PRINT = false;
	private static final PrintStream stream;

	static {
		try {
			stream = PRINT
					? new PrintStream("stmtLog.txt", AppConfig.DEFAULT_CHARSET)
					: NullPrintStream.NULL_PRINT_STREAM;
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private StatementPrinter() {
		throw new AssertionError("no init");
	}

	/**
	 * Prints the statement.
	 *
	 * @param stmt the statement
	 */
	public static void print(final String stmt) {
		stream.print(stmt);
		stream.println(";");
	}

	/**
	 * Prints the statement and its parameters.
	 *
	 * @param stmt   the statement
	 * @param params the parameters
	 */
	public static void print(final String stmt, final Map<String, Object> params) {
		printParams(params);
		print(stmt);
	}

	private static void printParams(final Map<String, Object> params) {
		params.forEach((k, v) -> stream.println(fmtParam(k, v)));
	}

	private static String fmtParam(final String k, final Object v) {
		return String.format(":param %s => %s;", k, v instanceof String ? "'" + v + "'" : v);
	}

}

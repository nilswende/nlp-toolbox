package de.fernuni_hagen.kn.nlp.db.neo4j;

import org.apache.commons.io.output.NullPrintStream;

import java.io.PrintStream;
import java.util.Map;

/**
 * @author Nils Wende
 */
public class StatementPrinter {

	private static final PrintStream stream;

	static {
		/*try {
			stream = new PrintStream("stmtLog.txt", Config.DEFAULT_CHARSET);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}*/
		stream = NullPrintStream.NULL_PRINT_STREAM;
	}

	public static void print(final String stmt) {
		stream.print(stmt);
		stream.println(";");
	}

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

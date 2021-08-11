package de.fernuni_hagen.kn.nlp.utils;

import de.fernuni_hagen.kn.nlp.UseCase;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Prints result objects.
 *
 * @author Nils Wende
 * @see UseCase.Result
 */
public class ResultPrinter {

	private final StringBuilder sb = new StringBuilder();

	/**
	 * Returns a string representation of this printer's content.
	 *
	 * @return a string representation of this printer's content
	 */
	@Override
	public String toString() {
		return sb.toString();
	}

	/**
	 * Prints the result.
	 *
	 * @param result a use case result
	 * @return a string representation of the result's content
	 */
	public static String print(final UseCase.Result result) {
		final var printer = new ResultPrinter();
		final var name = result.getUseCaseName();
		final var formatter = DateTimeFormatter.ofPattern("(HH:mm:ss)");
		printer.println(result.getStart().format(formatter), " Start ", name);
		result.toString(printer);
		printer.println(result.getEnd().format(formatter), " End ", name);
		final var duration = result.getDuration();
		printer.println(String.format("%s duration: %d s %d ms", name, duration.toSecondsPart(), duration.toMillisPart()));
		return printer.toString();
	}

	/**
	 * Prints the object and starts a new line.
	 *
	 * @param o Object
	 * @return this object
	 */
	public ResultPrinter println(final Object o) {
		sb.append(o).append(StringUtils.LF);
		return this;
	}

	/**
	 * Prints the objects and starts a new line.
	 *
	 * @param os Objects
	 * @return this object
	 */
	public ResultPrinter println(final Object... os) {
		Arrays.stream(os).forEach(sb::append);
		sb.append(StringUtils.LF);
		return this;
	}

	/**
	 * Prints the object and starts a new line.
	 *
	 * @param o Object
	 * @return this object
	 */
	public ResultPrinter print(final Object o) {
		println(o);
		return this;
	}

	/**
	 * Prints the map.
	 *
	 * @param map the map
	 * @return this object
	 */
	public ResultPrinter printMap(final Map<?, ?> map) {
		printfMap(map, "None", "%s: %s");
		return this;
	}

	/**
	 * Prints the map of maps.
	 *
	 * @param <K> inner key type
	 * @param <V> value type
	 * @param map the map
	 * @return this object
	 */
	public <K, V> ResultPrinter printMapMap(final Map<?, Map<K, V>> map) {
		printfMapMap(map, "None", "%s and %s: %s");
		return this;
	}

	/**
	 * Prints the format.
	 *
	 * @param format format string
	 * @param args   arguments
	 * @return this object
	 */
	public ResultPrinter printf(final String format, final Object... args) {
		println(String.format(format, args));
		return this;
	}

	/**
	 * Prints the collection.
	 *
	 * @param collection   the collection
	 * @param emptyMessage message if the collection is empty
	 * @param format       message for each collection entry
	 * @return this object
	 */
	public ResultPrinter printfCollection(final Collection<?> collection, final String emptyMessage, final String format) {
		if (collection.isEmpty()) {
			print(emptyMessage);
		} else {
			collection.forEach(e -> printf(format, e));
		}
		return this;
	}

	/**
	 * Prints the map.
	 *
	 * @param map          the map
	 * @param emptyMessage message if the map is empty
	 * @param format       message for each map entry
	 * @return this object
	 */
	public ResultPrinter printfMap(final Map<?, ?> map, final String emptyMessage, final String format) {
		if (map.isEmpty()) {
			print(emptyMessage);
		} else {
			map.forEach((k, v) -> printf(format, k, v));
		}
		return this;
	}

	/**
	 * Prints the map.
	 *
	 * @param map          the map
	 * @param nullMessage  message if the map is null
	 * @param emptyMessage message if the map is empty
	 * @param format       message for each map entry
	 * @return this object
	 */
	public ResultPrinter printfNullableMap(final Map<?, ?> map, final String nullMessage, final String emptyMessage, final String format) {
		if (map == null) {
			print(nullMessage);
		} else if (map.isEmpty()) {
			print(emptyMessage);
		} else {
			map.forEach((k, v) -> printf(format, k, v));
		}
		return this;
	}

	/**
	 * Prints the map of maps.
	 *
	 * @param <K>          inner key type
	 * @param <V>          value type
	 * @param map          the map
	 * @param emptyMessage message if the map is empty
	 * @param format       message for each inner map entry
	 * @return this object
	 */
	public <K, V> ResultPrinter printfMapMap(final Map<?, Map<K, V>> map, final String emptyMessage, final String format) {
		if (map.isEmpty()) {
			print(emptyMessage);
		} else {
			map.forEach((k1, m) -> m.forEach((k2, v) -> printf(format, k1, k2, v)));
		}
		return this;
	}

}

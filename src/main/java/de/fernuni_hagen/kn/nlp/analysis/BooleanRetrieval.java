package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.config.UseCaseConfig;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Boolean retrieval.
 *
 * @author Nils Wende
 */
public class BooleanRetrieval extends UseCase {

	private final Config config;

	public BooleanRetrieval(final Config config) {
		this.config = config;
	}

	@Override
	public void execute(final DBReader dbReader) {
		final Object result;
		if (config.getExpression().equalsIgnoreCase("and")) {
			result = and(dbReader);
		} else if (config.getExpression().equalsIgnoreCase("or")) {
			result = or(dbReader);
		} else if (config.getExpression().equalsIgnoreCase("not")) {
			result = not(dbReader);
		} else {
			throw new IllegalArgumentException("Unknown expression " + config.getExpression());
		}
		printNameAnd(result);
	}

	/**
	 * BooleanRetrieval config.
	 */
	public static class Config extends UseCaseConfig {
		private String expression;
		private List<String> query;

		public String getExpression() {
			return expression;
		}

		public List<String> getQuery() {
			return query;
		}
	}

	/**
	 * Uses boolean retrieval to find all documents that contain all of the query terms (AND retrieval).
	 *
	 * @param db DB
	 * @return a set of all documents that contain all search terms
	 */
	public Set<String> and(final DBReader db) {
		return or(db).entrySet().stream()
				.filter(e -> e.getValue() == config.getQuery().size())
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}

	/**
	 * Uses boolean retrieval to find all documents that contain any of the query terms (OR retrieval).
	 *
	 * @param db DB
	 * @return a map mapping each document to the total count of search terms that document contains
	 */
	public Map<String, Long> or(final DBReader db) {
		final var term2doc = db.getTermFrequencies();
		return config.getQuery().stream()
				.flatMap(t -> term2doc.getOrDefault(t, Map.of()).keySet().stream())
				.collect(Collectors.toMap(doc -> doc, doc -> 1L, Long::sum));
	}

	/**
	 * Uses boolean retrieval to find all documents that contain none of the query terms (NOT retrieval).
	 *
	 * @param db DB
	 * @return a set of all documents that contain none of the search terms
	 */
	public Set<String> not(final DBReader db) {
		final var doc2term = Maps.invertMapping(db.getTermFrequencies());
		return doc2term.entrySet().stream()
				.filter(e -> config.getQuery().stream().noneMatch(t -> e.getValue().containsKey(t)))
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}

}

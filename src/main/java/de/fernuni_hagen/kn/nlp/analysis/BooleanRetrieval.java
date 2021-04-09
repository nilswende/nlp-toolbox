package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.config.UseCase;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Boolean retrieval.
 *
 * @author Nils Wende
 */
public class BooleanRetrieval extends UseCase {

	private Type type;
	private List<String> query;

	private Result result;

	public class Result extends UseCase.Result {
		private final Map<String, Long> documents;

		Result(final Map<String, Long> documents) {
			this.documents = documents;
			printfMap(documents, "No matches found", "Document '%s' contains %s query terms");
		}

		public Map<String, Long> getDocuments() {
			return documents;
		}
	}

	/**
	 * The type of boolean retrieval.
	 */
	public enum Type {
		AND, OR, NOT
	}

	@Override
	public void execute(final DBReader dbReader) {
		final var map = findDocuments(dbReader);
		result = new Result(map);
	}

	private Map<String, Long> findDocuments(final DBReader dbReader) {
		switch (type) {
			case AND:
				return and(dbReader);
			case OR:
				return or(dbReader);
			case NOT:
				return not(dbReader);
			default:
				throw new IllegalArgumentException("Unknown expression " + type);
		}
	}

	/**
	 * Uses boolean retrieval to find all documents that contain all of the query terms (AND retrieval).
	 *
	 * @param db DB
	 * @return a set of all documents that contain all search terms
	 */
	private Map<String, Long> and(final DBReader db) {
		final long size = query.size();
		return or(db).entrySet().stream()
				.filter(e -> e.getValue() == size)
				.collect(Collectors.toMap(Map.Entry::getKey, e -> size));
	}

	/**
	 * Uses boolean retrieval to find all documents that contain any of the query terms (OR retrieval).
	 *
	 * @param db DB
	 * @return a map mapping each document to the total count of search terms that document contains
	 */
	private Map<String, Long> or(final DBReader db) {
		final var term2doc = db.getTermFrequencies();
		return query.stream()
				.flatMap(t -> term2doc.getOrDefault(t, Map.of()).keySet().stream())
				.collect(Collectors.groupingBy(doc -> doc, Collectors.counting()));
	}

	/**
	 * Uses boolean retrieval to find all documents that contain none of the query terms (NOT retrieval).
	 *
	 * @param db DB
	 * @return a set of all documents that contain none of the search terms
	 */
	private Map<String, Long> not(final DBReader db) {
		final var doc2term = Maps.invertMapping(db.getTermFrequencies());
		return doc2term.entrySet().stream()
				.filter(e -> query.stream().noneMatch(t -> e.getValue().containsKey(t)))
				.collect(Collectors.toMap(Map.Entry::getKey, e -> 0L));
	}

	@Override
	public Result getResult() {
		return result;
	}

	/**
	 * Set the type of boolean retrieval to use.
	 *
	 * @param type the type of boolean retrieval to use
	 * @return this object
	 */
	public BooleanRetrieval setType(final Type type) {
		this.type = type;
		return this;
	}

	/**
	 * Set the terms used for retrieval.
	 *
	 * @param query the terms used for retrieval
	 * @return this object
	 */
	public BooleanRetrieval setQuery(final List<String> query) {
		this.query = query;
		return this;
	}
}

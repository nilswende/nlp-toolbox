package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Boolean retrieval.
 *
 * @author Nils Wende
 */
public class BooleanRetrieval {

	/**
	 * Uses boolean retrieval to find all documents that contain all of the given terms (AND retrieval).
	 *
	 * @param query search query
	 * @param db    DB
	 * @return a list of all documents that contain all search terms
	 */
	public List<String> and(final List<String> query, final DBReader db) {
		return or(query, db).entrySet().stream()
				.filter(e -> e.getValue() == query.size())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	/**
	 * Uses boolean retrieval to find all documents that contain any of the given terms (OR retrieval).
	 *
	 * @param query search query
	 * @param db    DB
	 * @return a map mapping each document to the total count of search terms that document contains
	 */
	public Map<String, Long> or(final List<String> query, final DBReader db) {
		final var term2doc = db.getTermFrequencies();
		return query.stream()
				.flatMap(t -> term2doc.getOrDefault(t, Map.of()).keySet().stream())
				.collect(Collectors.toMap(
						doc -> doc,
						doc -> 1L,
						Long::sum
				));
	}

	/**
	 * Uses boolean retrieval to find all documents that contain none of the given terms (NOT retrieval).
	 *
	 * @param query search query
	 * @param db    DB
	 * @return a list of all documents that contain none of the search terms
	 */
	public List<String> not(final List<String> query, final DBReader db) {
		final var doc2term = Maps.invertMapping(db.getTermFrequencies());
		return doc2term.entrySet().stream()
				.filter(e -> query.stream().noneMatch(t -> e.getValue().containsKey(t)))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

}

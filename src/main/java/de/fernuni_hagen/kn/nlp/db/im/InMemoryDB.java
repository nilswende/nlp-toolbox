package de.fernuni_hagen.kn.nlp.db.im;

import java.util.Map;
import java.util.TreeMap;

/**
 * A simple in-memory database.
 *
 * @author Nils Wende
 */
enum InMemoryDB {
	INSTANCE; // enum singleton pattern

	private final Map<String, Values> data = new TreeMap<>();

	/**
	 * Adds a term to the database.
	 *
	 * @param term a term
	 */
	public void addTerm(final String term) {
		data.computeIfAbsent(term, t -> new Values()).count++;
	}

	/**
	 * Adds a directed relationship from term1 to term2.
	 *
	 * @param term1 a term
	 * @param term2 another term
	 */
	public void addDirectedRelationship(final String term1, final String term2) {
		data.get(term1).getCooccs().merge(term2, 1L, Long::sum);
	}

	/**
	 * Adds an undirected relationship between term1 and term2.
	 *
	 * @param term1 a term
	 * @param term2 another term
	 */
	public void addUndirectedRelationship(final String term1, final String term2) {
		addDirectedRelationship(term1, term2);
		addDirectedRelationship(term2, term1);
	}

	/**
	 * Removes all data from the database.
	 */
	public void deleteAll() {
		data.clear();
	}

	public Map<String, Values> getData() {
		return data;
	}

	public long getMaxSentencesCount() {
		return data.values().stream().mapToLong(Values::getCount).max().orElse(0L);
	}

	static class Values {
		private final Map<String, Long> cooccs = new TreeMap<>();
		private long count = 0;

		public long getCount() {
			return count;
		}

		public Map<String, Long> getCooccs() {
			return cooccs;
		}
	}

}
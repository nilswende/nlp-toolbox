package de.fernuni_hagen.kn.nlp.db.im;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A simple in-memory database.
 *
 * @author Nils Wende
 */
enum InMemoryDB {
	INSTANCE; // enum singleton pattern

	private final Map<String, Values> data = new TreeMap<>();
	private Path currentDoc;

	/**
	 * Starts a new document to write sentences from.
	 *
	 * @param path the document's file pah
	 */
	public void addDocument(final Path path) {
		currentDoc = path;
	}

	/**
	 * Adds a term to the database.
	 *
	 * @param term a term
	 */
	public void addTerm(final String term) {
		final var values = data.computeIfAbsent(term, t -> new Values());
		values.count++;
		values.getDocuments().add(currentDoc);
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

	/**
	 * Returns the database content.
	 *
	 * @return the database content
	 */
	public Map<String, Values> getData() {
		return data;
	}

	/**
	 * Returns the maximum number of sentences that contain any term.
	 *
	 * @return the maximum number of sentences that contain any term
	 */
	public long getMaxSentencesCount() {
		return data.values().stream().mapToLong(Values::getCount).max().orElse(0L);
	}

	/**
	 * All values a term in the database is mapped to.
	 */
	static class Values {
		private final Set<Path> documents = new HashSet<>();
		private final Map<String, Long> cooccs = new TreeMap<>();
		private long count = 0;

		public Set<Path> getDocuments() {
			return documents;
		}

		public Map<String, Long> getCooccs() {
			return cooccs;
		}

		public long getCount() {
			return count;
		}
	}

}

package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.config.Config;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A simple in-memory database.
 *
 * @author Nils Wende
 */
public class InMemoryDB {

	public static final String JSON_FILE = "data.json.gz";
	private final Map<String, Values> data;
	private String currentDoc;

	public InMemoryDB(final Config config) {
		final var path = config.getInMemoryDbDir().resolve(JSON_FILE);
		data = InMemoryDeserializer.deserialize(path);
		if (config.persistInMemoryDb()) {
			InMemorySerializer.persistOnShutdown(path, data);
		}
	}

	/**
	 * Starts a new document to write sentences from.
	 *
	 * @param path the document's file pah
	 */
	public void addDocument(final Path path) {
		currentDoc = formatPath(path);
	}

	/**
	 * Formats a Path to a normalized String.
	 *
	 * @param path Path
	 * @return String
	 */
	static String formatPath(final Path path) {
		return path.toAbsolutePath().toString();
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
		private final Set<String> documents = new TreeSet<>();
		private final Map<String, Long> cooccs = new TreeMap<>();
		private long count = 0;

		/**
		 * Returns the set of documents this term occurs in.
		 *
		 * @return the set of documents this term occurs in
		 */
		public Set<String> getDocuments() {
			return documents;
		}

		/**
		 * Returns the set of cooccurring terms along with the number of cooccurrences with this term.
		 *
		 * @return the set of cooccurring terms along with the number of cooccurrences with this term
		 */
		public Map<String, Long> getCooccs() {
			return cooccs;
		}

		/**
		 * Returns the total number of occurrences of this term.
		 *
		 * @return the total number of occurrences of this term
		 */
		public long getCount() {
			return count;
		}
	}

}

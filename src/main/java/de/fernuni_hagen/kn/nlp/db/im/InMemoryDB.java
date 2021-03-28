package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.config.AppConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple in-memory database.
 *
 * @author Nils Wende
 */
public class InMemoryDB {

	public static final String JSON_FILE = "data.json.gz";
	private final Content content;
	private String currentDoc;
	private int sentenceCount;

	public InMemoryDB(final AppConfig config) {
		final var path = config.getInMemoryDbDir().resolve(JSON_FILE);
		content = InMemoryDeserializer.deserialize(path);
		if (config.persistInMemoryDb()) {
			InMemorySerializer.persistOnShutdown(path, content);
		}
	}

	/**
	 * Starts a new document to write sentences from.
	 *
	 * @param fileName the document's original file name
	 */
	public void addDocument(final String fileName) {
		if (content.getData().values().stream().map(Values::getDocuments).anyMatch(d -> d.containsKey(fileName))) {
			System.out.println("no two input documents can have the same file name");
		}
		currentDoc = fileName;
		sentenceCount = 0;
	}

	/**
	 * Starts a new sentence to write terms from.
	 */
	public void addSentence() {
		sentenceCount++;
		content.getDoc2Sentences()
				.computeIfAbsent(currentDoc, x -> new ArrayList<>())
				.add(new ArrayList<>());
	}

	/**
	 * Adds a term to the database.
	 *
	 * @param term a term
	 */
	public void addTerm(final String term) {
		final var values = content.getData().computeIfAbsent(term, t -> new Values());
		values.documents.merge(currentDoc, 1L, Long::sum);
		content.getDoc2Sentences()
				.get(currentDoc)
				.get(sentenceCount - 1)
				.add(term);
	}

	/**
	 * Adds a directed relationship from term1 to term2.
	 *
	 * @param term1 a term
	 * @param term2 another term
	 */
	public void addDirectedRelationship(final String term1, final String term2) {
		content.getData().get(term1).getCooccs().merge(term2, 1L, Long::sum);
	}

	/**
	 * Adds an undirected relationship between term1 and term2.
	 *
	 * @param term1 a term
	 * @param term2 another term
	 */
	public void addUndirectedRelationship(final String term1, final String term2) {
		addDirectedRelationship(term1, term2);
		if (!term1.equals(term2)) {
			addDirectedRelationship(term2, term1);
		}
	}

	/**
	 * Removes all data from the database.
	 */
	public void deleteAll() {
		content.clear();
	}

	/**
	 * Returns the database content.
	 *
	 * @return the database content
	 */
	public Map<String, Values> getData() {
		return content.getData();
	}

	public Map<String, List<List<String>>> getDoc2Sentences() {
		return content.getDoc2Sentences();
	}

	/**
	 * Returns the maximum number of sentences that contain the same term.
	 *
	 * @return the maximum number of sentences that contain the same term
	 */
	public long getMaxSentencesCount() {
		return content.getData().values().stream().mapToLong(Values::getCount).max().orElse(0L);
	}

	/**
	 * Returns the total number of sentences.
	 *
	 * @return the total number of sentences
	 */
	public long getSentencesCount() {
		return content.getDoc2Sentences().values().stream().mapToLong(List::size).sum();
	}

	/**
	 * All values a term in the database is mapped to.
	 */
	static class Values {
		private final Map<String, Long> documents = new TreeMap<>();
		private final Map<String, Long> cooccs = new TreeMap<>();

		/**
		 * Returns the set of documents this term occurs in.
		 *
		 * @return Map Document -> Count
		 */
		public Map<String, Long> getDocuments() {
			return documents;
		}

		/**
		 * Returns the set of cooccurring terms along with the number of cooccurrences with this term.
		 *
		 * @return Map Coocc -> Count
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
			return documents.values().stream().mapToLong(l -> l).sum();
		}
	}

	static class Content {
		private Map<String, Values> data;
		private Map<String, List<List<String>>> doc2Sentences;

		public static Content init() {
			final var content = new Content();
			content.data = new TreeMap<>();
			content.doc2Sentences = new TreeMap<>();
			return content;
		}

		public Map<String, Values> getData() {
			return data;
		}

		public Map<String, List<List<String>>> getDoc2Sentences() {
			return doc2Sentences;
		}

		public void clear() {
			data.clear();
			doc2Sentences.clear();
		}
	}

}

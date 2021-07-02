package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.DB;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A simple in-memory database.
 *
 * @author Nils Wende
 */
public class InMemoryDB implements DB {

	public static final String JSON_FILE = "data.json.gz";
	private final AppConfig config;
	private final Content content;
	private String currentDoc;
	private int sentenceCount;

	/**
	 * Starts the in-memory database.
	 *
	 * @param config AppConfig
	 */
	public InMemoryDB(final AppConfig config) {
		this.config = config;
		content = InMemoryDeserializer.deserialize(getDbFilePath(config));
	}

	private Path getDbFilePath(final AppConfig config) {
		return config.getInMemoryDbDir().resolve(JSON_FILE);
	}

	/**
	 * Starts a new document to write sentences from.
	 *
	 * @param fileName the document's original file name
	 */
	public void addDocument(final String fileName) {
		if (content.getData().values().stream().map(Values::getDocuments).anyMatch(d -> d.containsKey(fileName))) {
			throw new IllegalArgumentException("no two input documents can have the same file name");
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
		values.sentences.add(currentSentence());
		content.getDoc2Sentences()
				.get(currentDoc)
				.get(sentenceCount - 1)
				.add(term);
	}

	private String currentSentence() {
		return currentDoc + sentenceCount;
	}

	/**
	 * Adds a directed relationship from term1 to term2.
	 *
	 * @param term1 a term
	 * @param term2 another term
	 */
	public void addDirectedRelationship(final String term1, final String term2) {
		content.getData().get(term1).getCooccsPerSentence()
				.computeIfAbsent(term2, x -> new TreeMap<>())
				.merge(currentSentence(), 1L, Long::sum);
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

	/**
	 * Returns the mapping of all documents to their sentences.
	 *
	 * @return the mapping of all documents to their sentences
	 */
	public Map<String, List<List<String>>> getDoc2Sentences() {
		return content.getDoc2Sentences();
	}

	/**
	 * Returns the maximum number of sentences that contain the same term.
	 *
	 * @return the maximum number of sentences that contain the same term
	 */
	public long getMaxSentenceCount() {
		return content.getData().values().stream()
				.mapToLong(Values::getSentenceCount)
				.max().orElse(0L);
	}

	/**
	 * Returns the maximum number of occurrences of a term.
	 *
	 * @return the maximum number of occurrences of a term
	 */
	public long getMaxTermCount() {
		return content.getData().values().stream()
				.mapToLong(Values::getCount)
				.max().orElse(0L);
	}

	/**
	 * Returns the total number of sentences.
	 *
	 * @return the total number of sentences
	 */
	public long getSentencesCount() {
		return content.getDoc2Sentences().values().stream()
				.mapToLong(List::size)
				.sum();
	}

	@Override
	public void shutdown() {
		if (config.persistInMemoryDb()) {
			InMemorySerializer.persist(getDbFilePath(config), content);
		}
	}

	/**
	 * All values a term in the database is mapped to.
	 */
	static class Values {
		private final Map<String, Long> documents = new TreeMap<>();
		private final Set<String> sentences = new TreeSet<>();
		private final Map<String, Map<String, Long>> cooccs = new TreeMap<>();

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
			return cooccs.entrySet().stream()
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							e -> e.getValue().values().stream().mapToLong(l -> l).sum()
					));
		}

		/**
		 * Returns the set of cooccurring terms along with the number of cooccurrences with this term.
		 *
		 * @return Map Coocc -> Count as Double
		 */
		public Map<String, Double> getCooccsAsDouble() {
			return cooccs.entrySet().stream()
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							e -> e.getValue().values().stream().mapToDouble(l -> l).sum()
					));
		}

		/**
		 * Returns the set of cooccurring terms along with the number of cooccurrences with this term in distinct sentences.
		 *
		 * @return Map Coocc -> distinct Count
		 */
		public Map<String, Long> getDistinctCooccs() {
			return cooccs.entrySet().stream()
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							e -> (long) e.getValue().values().size()
					));
		}

		/**
		 * Returns the set of cooccurring terms along with the number of cooccurrences with this term per sentence.
		 *
		 * @return Map Coocc -> Map Sentence -> Count
		 */
		public Map<String, Map<String, Long>> getCooccsPerSentence() {
			return cooccs;
		}

		/**
		 * Returns the total number of sentences that contain this term.
		 *
		 * @return the total number of sentences that contain this term
		 */
		public long getSentenceCount() {
			return sentences.size();
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

	/**
	 * All database content.
	 */
	static class Content {
		private Map<String, Values> data;
		private Map<String, List<List<String>>> doc2Sentences;

		/**
		 * Creates an empty database.
		 *
		 * @return an empty database
		 */
		public static Content init() {
			final var content = new Content();
			content.data = new TreeMap<>();
			content.doc2Sentences = new TreeMap<>();
			return content;
		}

		/**
		 * Returns the values a term in the database is mapped to.
		 *
		 * @return the values a term in the database is mapped to
		 */
		public Map<String, Values> getData() {
			return data;
		}

		/**
		 * Returns the mapping of all documents to their sentences.
		 *
		 * @return the mapping of all documents to their sentences
		 */
		public Map<String, List<List<String>>> getDoc2Sentences() {
			return doc2Sentences;
		}

		/**
		 * Clears the database.
		 */
		public void clear() {
			data.clear();
			doc2Sentences.clear();
		}
	}

}

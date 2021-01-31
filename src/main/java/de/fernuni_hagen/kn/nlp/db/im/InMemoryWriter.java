package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.preprocessing.Sentence;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements writing to the in-memory database.
 *
 * @author Nils Wende
 */
public class InMemoryWriter implements DBWriter {

	private final InMemoryDB db;

	public InMemoryWriter(final InMemoryDB db) {
		this.db = db;
	}

	@Override
	public void deleteAll() {
		db.deleteAll();
	}

	@Override
	public void addDocument(final Path path) {
		db.addDocument(path);
	}

	@Override
	public void addSentence(final Sentence sentence) {
		final var distinctTerms = sentence.getContent().distinct().collect(Collectors.toList());
		addTerms(distinctTerms);
		addRelationships(distinctTerms);
	}

	private void addTerms(final List<String> distinctTerms) {
		distinctTerms.forEach(db::addTerm);
	}

	private void addRelationships(final List<String> distinctTerms) {
		for (int i = 0; i < distinctTerms.size(); i++) {
			final var term1 = distinctTerms.get(i);
			for (int j = i + 1; j < distinctTerms.size(); j++) {
				final var term2 = distinctTerms.get(j);
				db.addUndirectedRelationship(term1, term2);
			}
		}
	}

}

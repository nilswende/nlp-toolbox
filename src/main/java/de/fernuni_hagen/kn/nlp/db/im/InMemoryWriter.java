package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.DBWriter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Implements writing to the in-memory database.
 *
 * @author Nils Wende
 */
public class InMemoryWriter implements DBWriter {

	@Override
	public void deleteAll() {
		InMemoryDB.INSTANCE.deleteAll();
	}

	@Override
	public void addDocument(final Path path) {
		InMemoryDB.INSTANCE.addDocument(path);
	}

	@Override
	public void addSentence(final List<String> terms) {
		final var distinctTerms = new ArrayList<>(new HashSet<>(terms));
		addTerms(distinctTerms);
		addRelationships(distinctTerms);
	}

	private void addTerms(final List<String> distinctTerms) {
		distinctTerms.forEach(InMemoryDB.INSTANCE::addTerm);
	}

	private void addRelationships(final List<String> distinctTerms) {
		for (int i = 0; i < distinctTerms.size(); i++) {
			final var term1 = distinctTerms.get(i);
			for (int j = i + 1; j < distinctTerms.size(); j++) {
				final var term2 = distinctTerms.get(j);
				InMemoryDB.INSTANCE.addUndirectedRelationship(term1, term2);
			}
		}
	}

}

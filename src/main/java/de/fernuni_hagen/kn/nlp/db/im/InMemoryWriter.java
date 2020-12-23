package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.DBWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Nils Wende
 */
public class InMemoryWriter implements DBWriter {

	@Override
	public void addDocument(final File file) {
		// do nothing
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

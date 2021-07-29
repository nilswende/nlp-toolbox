package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.file.Exporter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Implements writing to the in-memory database.
 *
 * @author Nils Wende
 */
public class InMemoryWriter implements DBWriter {

	private final Exporter exporter = new Exporter("data/output/imWriter.txt", false);
	private final InMemoryDB db;

	public InMemoryWriter(final InMemoryDB db) {
		this.db = db;
	}

	@Override
	public void deleteAll() {
		db.deleteAll();
	}

	@Override
	public void addDocument(final String name) {
		db.addDocument(name);
	}

	@Override
	public void addSentence(final List<String> terms) {
		db.addSentence();
		exporter.println(() -> String.join(StringUtils.SPACE, terms));
		addTerms(terms);
		addRelationships(terms);
	}

	private void addTerms(final List<String> terms) {
		terms.forEach(db::addTerm);
	}

	private void addRelationships(final List<String> terms) {
		for (int i = 0; i < terms.size(); i++) {
			final var term1 = terms.get(i);
			for (int j = i + 1; j < terms.size(); j++) {
				final var term2 = terms.get(j);
				if (!term1.equals(term2)) {
					db.addUndirectedRelationship(term1, term2);
				}
			}
		}
	}

}

package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;

/**
 * Initializes the DB before the first test class and resets it for each test.
 *
 * @author Nils Wende
 */
public abstract class DBTest {

	protected DBReader reader = getDbFactory().getReader();
	protected DBWriter writer = getDbFactory().getWriter();

	@BeforeEach
	void beforeEach() {
		writer.deleteAll();
		writer.addDocument(Path.of("1"));
	}

	@AfterEach
	void afterEach() {
		writer.deleteAll();
	}

	protected abstract DBFactory getDbFactory();

}

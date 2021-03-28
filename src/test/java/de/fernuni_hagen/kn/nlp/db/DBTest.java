package de.fernuni_hagen.kn.nlp.db;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.DBWriter;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import org.junit.jupiter.api.AfterEach;

import java.nio.file.Path;

/**
 * Initializes the DB before the first test class and resets it after each test.
 *
 * @author Nils Wende
 */
public abstract class DBTest {

	protected DBReader reader = getDbFactory().getReader();
	protected DBWriter writer = getDbFactory().getWriter();

	{
		writer.addDocument(Path.of(""));
	}

	@AfterEach
	void tearDown() {
		writer.deleteAll();
	}

	protected abstract DBFactory getDbFactory();

}

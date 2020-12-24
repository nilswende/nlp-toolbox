package de.fernuni_hagen.kn.nlp.db.factory;

import de.fernuni_hagen.kn.nlp.db.DBTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Nils Wende
 */
public class DBFactoryTest extends DBTest {

	@Test
	void getReader() {
		assertNotNull(reader);
	}

	@Test
	void getWriter() {
		assertNotNull(writer);
	}

	@Test
	void getDb() {
		assertNotNull(DBFactory.instance().getDb());
	}

	@Test
	void instance() {
		assertNotNull(DBFactory.instance());
	}

	@Test
	void init() {
		assertThrows(AssertionError.class, () -> DBFactory.init(null));
	}

}

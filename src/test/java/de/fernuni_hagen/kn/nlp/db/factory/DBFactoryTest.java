package de.fernuni_hagen.kn.nlp.db.factory;

import de.fernuni_hagen.kn.nlp.db.im.DBTestIm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Nils Wende
 */
public class DBFactoryTest extends DBTestIm {

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
		assertNotNull(getDbFactory().getDb());
	}

	@Test
	void instance() {
		assertNotNull(getDbFactory());
	}

	@Test
	void from() {
		assertThrows(NullPointerException.class, () -> DBFactory.from(null));
	}

}

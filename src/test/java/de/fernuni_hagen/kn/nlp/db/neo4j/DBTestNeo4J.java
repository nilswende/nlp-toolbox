package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.DBTest;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import org.mockito.Mockito;

import java.nio.file.Path;

/**
 * @author Nils Wende
 */
public abstract class DBTestNeo4J extends DBTest {

	protected static final DBFactory dbFactory;

	static {
		final var mock = Mockito.mock(AppConfig.class);
		Mockito.when(mock.getDb()).thenReturn(AppConfig.DB_NEO4J);
		final var path = Path.of("test", "neo4j");
		System.out.println(path.toAbsolutePath());
		Mockito.when(mock.getNeo4JDbDir()).thenReturn(path);
		dbFactory = DBFactory.from(mock);
		//Runtime.getRuntime().addShutdownHook(new Thread(this::deleteDbDir));
	}

	protected void printAllTerms() {
		try (final var tx = ((Neo4J) getDbFactory().getDb()).getGraphDb().beginTx()) {
			final var nodes = tx.getAllNodes();
			Neo4JUtils.stream(nodes).map(EntityFormatter::formatNode).forEach(System.out::println);
			System.out.println();
		}
	}

	@Override
	protected DBFactory getDbFactory() {
		return dbFactory;
	}
}

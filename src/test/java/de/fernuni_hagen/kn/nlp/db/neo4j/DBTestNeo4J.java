package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.db.DBTest;
import de.fernuni_hagen.kn.nlp.db.factory.DBFactory;
import de.fernuni_hagen.kn.nlp.utils.Utils;
import org.mockito.Mockito;
import org.neo4j.graphdb.Transaction;

import java.nio.file.Path;

/**
 * @author Nils Wende
 */
public class DBTestNeo4J extends DBTest {

	private static final DBFactory dbFactory;

	static {
		final var mock = Mockito.mock(AppConfig.class);
		Mockito.when(mock.getDb()).thenReturn(AppConfig.DbType.NEO4J);
		final var path = Path.of("test", "neo4j");
		System.out.println(path.toAbsolutePath());
		Mockito.when(mock.getNeo4JDbDir()).thenReturn(path);
		dbFactory = DBFactory.from(mock);
		//Runtime.getRuntime().addShutdownHook(new Thread(this::deleteDbDir));
	}

	protected void printAllTerms() {
		try (final var tx = ((Neo4J) getDbFactory().getDb()).getGraphDb().beginTx()) {
			final var nodes = tx.getAllNodes();
			Utils.stream(nodes).map(EntityFormatter::formatNode).forEach(System.out::println);
			System.out.println();
		}
	}

	public void printAllRelationships() {
		try (final Transaction tx = ((Neo4J) getDbFactory().getDb()).getGraphDb().beginTx()) {
			tx.getAllRelationships().stream()
					.map(EntityFormatter::formatRelationship)
					.forEach(System.out::println);
			System.out.println();
		}
	}

	@Override
	protected DBFactory getDbFactory() {
		return dbFactory;
	}
}

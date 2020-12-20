package de.fernuni_hagen.kn.nlp.db.neo4j;

import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Neo4j CSV export.<br>
 * <a href="https://neo4j.com/developer/guide-import-csv/">Importing CSV Data into Neo4j</a><br>
 *
 * @author Nils Wende
 */
public class CsvExporter {

	private final GraphDatabaseService graphDb;

	public CsvExporter() {
		graphDb = Neo4J.instance().getGraphDb();
	}

	public void export() {
		exportNodes();
		exportRelationships();
	}

	private void exportNodes() {
		try (final Transaction tx = graphDb.beginTx()) {
			for (final Label label : tx.getAllLabels()) {
				try (final var writer = new OutputStreamWriter(new FileOutputStream("export/nodes" + label + ".csv"), StandardCharsets.UTF_8)) {
					final var nodes = tx.findNodes(label);
					boolean first = true;
					while (nodes.hasNext()) {
						final var node = nodes.next();
						first = exportEntity(node, first, writer);
					}
				} catch (final IOException e) {
					throw new UncheckedException(e);
				}
			}
		}
	}

	private boolean exportEntity(final Entity entity, boolean first, final OutputStreamWriter writer) throws IOException {
		final Function<Map.Entry<String, Object>, Object> mapper;
		if (first) {
			first = false;
			mapper = Map.Entry::getKey;
		} else {
			mapper = Map.Entry::getValue;
		}
		final String line = entity.getAllProperties()
				.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(mapper)
				.map(Object::toString)
				.collect(Collectors.joining("\t"));
		writer.write(line);
		writer.write('\n');
		return first;
	}

	//TODO format for header and body
	private void exportRelationships() {
		try (final Transaction tx = graphDb.beginTx()) {
			for (final RelationshipType type : tx.getAllRelationshipTypes()) {
				try (final var writer = new OutputStreamWriter(new FileOutputStream("export/relationships" + type + ".csv"), StandardCharsets.UTF_8)) {
					boolean first = true;
					for (final Relationship relationship : tx.getAllRelationships()) {
						if (relationship.isType(type)) {
							final Function<Map.Entry<String, Object>, Object> mapper;
							if (first) {
								first = false;
								mapper = Map.Entry::getKey;
								final String line = relationship.getAllProperties()
										.entrySet().stream()
										.sorted(Map.Entry.comparingByKey())
										.map(mapper)
										.map(Object::toString)
										.collect(Collectors.joining("\t"));
								writer.write(line);
								writer.write('\n');
							} else {
								mapper = Map.Entry::getValue;
								final String line = relationship.getAllProperties()
										.entrySet().stream()
										.sorted(Map.Entry.comparingByKey())
										.map(mapper)
										.map(Object::toString)
										.collect(Collectors.joining("\t"));
								writer.write(line);
								writer.write('\n');
							}
						}
					}
				} catch (final IOException e) {
					throw new UncheckedException(e);
				}
			}
		}
	}

}

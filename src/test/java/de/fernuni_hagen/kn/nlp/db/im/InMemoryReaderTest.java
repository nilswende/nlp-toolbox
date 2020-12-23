package de.fernuni_hagen.kn.nlp.db.im;

import de.fernuni_hagen.kn.nlp.math.DirectedWeightingFunctions;
import de.fernuni_hagen.kn.nlp.math.WeightingFunctions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nils Wende
 */
class InMemoryReaderTest {

	InMemoryReader reader = new InMemoryReader();
	InMemoryWriter writer = new InMemoryWriter();

	@Test
	void getSignificances() {
		final var input = List.of("art", "competition", "game", "year");
		writer.addSentence(input);

		final var significances = reader.getSignificances(WeightingFunctions.NONE);
		assertEquals(Set.copyOf(input), significances.keySet());
		significances.forEach((k, v) -> {
			final var terms = new TreeSet<>(input);
			terms.remove(k);
			final var cooccs = v.keySet();
			assertEquals(terms, cooccs);
		});
	}

	@Test
	void testDirectedSignificances() {
		final var input = List.of("art", "competition", "game", "year");
		writer.addSentence(input);

		final var significances = reader.getSignificances(DirectedWeightingFunctions.NONE);
		assertOneDirectedRelationshipBetweenTwoNodes(significances);
	}

	private void assertOneDirectedRelationshipBetweenTwoNodes(Map<String, Map<String, Double>> significances) {
		significances.forEach(
				(k, v) -> v.keySet().forEach(
						coocc -> assertTrue(!significances.containsKey(coocc) || !significances.get(coocc).containsKey(k))
				));
	}

	@AfterEach
	void tearDown() {
		InMemoryDB.INSTANCE.deleteAll();
	}

}

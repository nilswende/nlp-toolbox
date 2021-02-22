package de.fernuni_hagen.kn.nlp.math;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class WeightingFunctionTest {

	static final Pair<String, Integer> rechtfertigung = Pair.of("Rechtfertigung", 1);
	static final Pair<String, Integer> profit = Pair.of("Profit", 3);
	static final Pair<String, Integer> vertrag = Pair.of("Vertrag", 1);
	static final Pair<String, Integer> fuehren = Pair.of("führen", 1);
	static final Pair<String, Integer> weg = Pair.of("Weg", 2);
	static final Pair<String, Integer> riskant = Pair.of("riskant", 1);
	static final Pair<String, Integer> gross = Pair.of("groß", 1);
	static final List<Pair<Pair<Pair<String, Integer>, Pair<String, Integer>>, Integer>> termSatzMatrix = List.of(
			Pair.of(Pair.of(rechtfertigung, profit), 1),
			Pair.of(Pair.of(profit, fuehren), 1),
			Pair.of(Pair.of(profit, weg), 2),
			Pair.of(Pair.of(profit, riskant), 1),
			Pair.of(Pair.of(profit, gross), 1),
			Pair.of(Pair.of(fuehren, weg), 1),
			Pair.of(Pair.of(weg, riskant), 1),
			Pair.of(Pair.of(riskant, gross), 1));
	static final int k = 4;

	@ParameterizedTest
	@MethodSource
	void test(final WeightingFunction function, final List<Pair<Pair<Pair<String, Integer>, Pair<String, Integer>>, Double>> expectedTermTermMatrix) {
		final var termTermMatrix = termSatzMatrix.stream().map(p -> {
			final var ki = p.getLeft().getLeft().getRight();
			final var kj = p.getLeft().getRight().getRight();
			final var kij = p.getRight();
			final var sig = function.calculate(ki, kj, kij, k, 0);
			return Pair.of(p.getLeft(), sig);
		}).collect(Collectors.toList());

		assertEquals(expectedTermTermMatrix.size(), termTermMatrix.size());
		for (int i = 0; i < expectedTermTermMatrix.size(); i++) {
			final var expected = expectedTermTermMatrix.get(i);
			final var actual = termTermMatrix.get(i);
			assertEquals(expected.getLeft(), actual.getLeft(), expected.getLeft() + " <!=> " + actual.getLeft());
			assertEquals(expected.getRight(), actual.getRight(), .001, expected.getLeft().toString());
		}
	}

	static Stream<Arguments> test() {
		return Stream.of(
				arguments(WeightingFunction.JACCARD, List.of(
						Pair.of(Pair.of(rechtfertigung, profit), .333),
						Pair.of(Pair.of(profit, fuehren), .333),
						Pair.of(Pair.of(profit, weg), .666),
						Pair.of(Pair.of(profit, riskant), .333),
						Pair.of(Pair.of(profit, gross), .333),
						Pair.of(Pair.of(fuehren, weg), .5),
						Pair.of(Pair.of(weg, riskant), .5),
						Pair.of(Pair.of(riskant, gross), 1.0))),
				arguments(WeightingFunction.DICE, List.of(
						Pair.of(Pair.of(rechtfertigung, profit), .5),
						Pair.of(Pair.of(profit, fuehren), .5),
						Pair.of(Pair.of(profit, weg), .8),
						Pair.of(Pair.of(profit, riskant), .5),
						Pair.of(Pair.of(profit, gross), .5),
						Pair.of(Pair.of(fuehren, weg), .666),
						Pair.of(Pair.of(weg, riskant), .666),
						Pair.of(Pair.of(riskant, gross), 1.0)))
		);
	}

}

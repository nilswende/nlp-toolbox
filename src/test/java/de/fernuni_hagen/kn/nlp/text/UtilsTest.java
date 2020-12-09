package de.fernuni_hagen.kn.nlp.text;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nils Wende
 */
class UtilsTest {

	@ParameterizedTest
	@MethodSource
	void countChars(final String s) {
		assertEquals(s.length(), Utils.countChars(new StringReader(s)));
	}

	static List<Arguments> countChars() {
		return Arrays.asList(//
				Arguments.of(""),
				Arguments.of("abc")
		);
	}

}

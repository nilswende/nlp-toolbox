package de.fernuni_hagen.kn.nlp.input;

import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class LazySentenceSupplierTest {

	private static File tempFile;

	@BeforeAll
	static void setUp() {
		tempFile = FileHelper.createTempFile(".test");
	}

	@ParameterizedTest
	@MethodSource
	void get(final List<String> sentences) {
		final String input = String.join("", sentences);
		try {
			FileUtils.write(tempFile, input, Config.DEFAULT_CHARSET);
			final var strings = new ArrayList<String>();
			try (final var sentenceSupplier = new LazySentenceSupplier(tempFile, Locale.ENGLISH)) {
				for (char[] s; (s = sentenceSupplier.get()) != null; ) {
					strings.add(String.valueOf(s));
				}
			}
			assertEquals(sentences, strings);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	static Stream<Arguments> get() {
		return Stream.of(
				arguments(List.of()),
				arguments(List.of("Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948. ")),
				arguments(List.of("Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948. ",
						"The competitions were part of the original intention of the Olympic Movement's founder, Pierre de Frédy, Baron de Coubertin. ")),
				arguments(List.of("Art competitions formed part of the modern Olympic Games during its early years, from 1912 to 1948. ",
						"The competitions were part of the original intention of the Olympic Movement's founder, Pierre de Frédy, Baron de Coubertin. ",
						"Medals were awarded for works of art inspired by sport, divided into five categories: architecture, literature, music, painting, and sculpture. "))
		);
	}

	@AfterAll
	static void tearDown() {
		FileHelper.delete(tempFile);
	}

}

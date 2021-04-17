package de.fernuni_hagen.kn.nlp.preprocessing.textual.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Nils Wende
 */
class Text2SatzSentenceSplitterTest {

	@ParameterizedTest
	@MethodSource
	void test(final CharSequence input, final List<String> expected) {
		final var actual = new Text2SatzSentenceSplitter().split(input);
		assertEquals(expected, actual);
	}

	static Stream<Arguments> test() {
		return Stream.of(//
				arguments("abc abc", List.of("abc abc")),
				arguments("abc\nabc", List.of("abc abc")),
				arguments("abc  abc", List.of("abc abc")),
				arguments("abc \nabc", List.of("abc abc")),
				arguments("abc\n abc", List.of("abc abc")),
				arguments("abc\n\nabc", List.of("abc", "abc")),
				arguments("abc\n \nabc", List.of("abc", "abc")),
				arguments("abc\n \n \nabc", List.of("abc", "abc")),
				arguments("abc \n\nabc", List.of("abc", "abc")),
				arguments("abc \n abc", List.of("abc", "abc")),
				// CRLF
				arguments("abc\r\nabc", List.of("abc abc")),
				// Tab
				arguments("abc\tabc", List.of("abc abc")),
				arguments("abc \tabc", List.of("abc abc")),
				// start of text
				arguments(" abc\nabc", List.of(" abc abc")),
				arguments("\nabc\nabc", List.of("abc abc")),
				arguments("\n abc\nabc", List.of("abc abc")),
				arguments("\n\n abc\nabc", List.of("abc abc")),
				// hyphen
				arguments("abc-abc", List.of("abc-abc")),
				arguments("abc-\n \nabc", List.of("abc-abc")),
				arguments("abc -\n\nabc", List.of("abc -abc")),
				arguments("abc- abc", List.of("abc- abc")),
				arguments("abc-\nabc", List.of("abcabc")), // seems language-specific
				arguments("abc-\nAbc", List.of("abc-Abc")),
				arguments("abc-\n\nabc", List.of("abc-abc")),
				arguments("abc--\n\nabc", List.of("abc--abc")),
				arguments("abc\n-abc", List.of("abc -abc")),
				arguments("abc\n \n-abc", List.of("abc", "-abc")),
				arguments("abc \n\n-abc", List.of("abc", "-abc")),
				arguments("abc \n\nabc-\n\nabc", List.of("abc", "abc-abc")),
				// end of sentence
				arguments("abc. ", List.of("abc.")),
				arguments("abc\nabc. ", List.of("abc abc.")),
				arguments("abc\nabc.\n\n", List.of("abc abc."))
		);
	}

}

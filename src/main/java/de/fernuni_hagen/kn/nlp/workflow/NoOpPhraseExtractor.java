package de.fernuni_hagen.kn.nlp.workflow;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Does not extract phrases from a text.
 *
 * @author Nils Wende
 */
public class NoOpPhraseExtractor implements PhraseExtractor {

	@Override
	public List<Pair<String, List<String>>> extractPhrases(final Locale locale, final List<String> sentences) {
		return sentences.stream().map(s -> Pair.of(s, List.<String>of())).collect(Collectors.toList());
	}

}

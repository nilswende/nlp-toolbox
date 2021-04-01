package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.StopWordRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.TaggedTerm;
import te.utils.ExternalData;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.fernuni_hagen.kn.nlp.preprocessing.linguistic.PreprocessingUtils.cast;

/**
 * Removes stop words from a sentence using the ASV library.
 *
 * @author Nils Wende
 */
public class ASVStopWordRemover implements StopWordRemover {

	private final Locale locale;
	private final Set<String> stopWords;

	/**
	 * Constructor.
	 *
	 * @param locale      the sentence's language
	 * @param asvLanguage the language constant defined by the ASV library
	 */
	public ASVStopWordRemover(final Locale locale, final int asvLanguage) {
		this.locale = locale;
		final ExternalData ed = ExternalData.getInstance(asvLanguage);
		final Set<String> set = cast(ed.getStopWordMap());
		stopWords = set.stream()
				.map(this::normalize)
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Stream<TaggedTerm> apply(final Stream<TaggedTerm> sentence) {
		return sentence.filter(w -> !stopWords.contains(normalize(w.getTerm())));
	}

	private String normalize(final String word) {
		return word.toLowerCase(locale);
	}

}

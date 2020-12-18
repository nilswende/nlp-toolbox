package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.preprocessing.impl.DEBaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.ENBaseFormReducer;

import java.util.Locale;
import java.util.stream.Stream;

/**
 * Reduces the terms of a sentence to their base forms.
 *
 * @author Nils Wende
 */
public interface BaseFormReducer extends PreprocessingStep {

	/**
	 * Reduces a term to its base form.
	 *
	 * @param sentence the terms of a sentence
	 * @return the reduced terms
	 */
	@Override
	Stream<TaggedWord> apply(Stream<TaggedWord> sentence);

	/**
	 * Factory: Creates a BaseFormReducer based on the given locale.
	 *
	 * @param locale the language of the sentences the reducer will be applied to
	 * @return a new BaseFormReducer
	 */
	static BaseFormReducer from(final Locale locale) {
		switch (locale.getLanguage()) {
			case "de":
				return new DEBaseFormReducer();
			case "en":
				return new ENBaseFormReducer();
			default:
				throw new IllegalArgumentException("Unsupported locale: " + locale);
		}
	}

}

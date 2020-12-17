package de.fernuni_hagen.kn.nlp.workflow;

import de.fernuni_hagen.kn.nlp.workflow.impl.DEBaseFormReducer;
import de.fernuni_hagen.kn.nlp.workflow.impl.ENBaseFormReducer;

import java.util.Locale;
import java.util.stream.Stream;

/**
 * Reduces the terms of a sentence to their base forms.
 *
 * @author Nils Wende
 */
@FunctionalInterface
public interface BaseFormReducer {

	/**
	 * Reduces a term to its base form.
	 *
	 * @param sentence the terms of a sentence
	 * @return the reduced terms
	 */
	Stream<String> reduce(Stream<String> sentence);

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

package de.fernuni_hagen.kn.nlp.preprocessing.factory;

import de.fernuni_hagen.kn.nlp.input.impl.JLanILanguageExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.AbbreviationFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.BaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.NounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.PhraseExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.SentenceExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.StopWordFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.Tagger;
import te.utils.Parameters;

import java.io.File;

/**
 * Defines a factory for the components used in preprocessing (Abstract Factory Pattern).
 *
 * @author Nils Wende
 */
public interface PreprocessingFactory {

	AbbreviationFilter createAbbreviationFilter();

	BaseFormReducer createBaseFormReducer();

	NounFilter createNounFilter();

	PhraseExtractor createPhraseExtractor();

	SentenceExtractor createSentenceExtractor();

	StopWordFilter createStopWordFilter();

	Tagger createTagger();

	static PreprocessingFactory from(final File file) {
		final var locale = new JLanILanguageExtractor().extract(file);
		switch (locale.getLanguage()) {
			case "de":
				return new DEFactory(Parameters.DE);
			case "en":
				return new ENFactory(Parameters.EN);
			default:
				throw new IllegalArgumentException("Unsupported locale: " + locale);
		}
	}

}

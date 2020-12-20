package de.fernuni_hagen.kn.nlp.preprocessing.factory;

import de.fernuni_hagen.kn.nlp.input.impl.JLanILanguageExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.AbbreviationFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.BaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.CaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.NounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.PhraseExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.SentenceExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.StopWordFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.Tagger;

import java.io.File;

/**
 * Defines a factory for the components used in preprocessing (Abstract Factory Pattern).
 *
 * @author Nils Wende
 */
public interface PreprocessingFactory {

	AbbreviationFilter createAbbreviationFilter();

	BaseFormReducer createBaseFormReducer();

	CaseNormalizer createCaseNormalizer();

	NounFilter createNounFilter();

	PhraseExtractor createPhraseExtractor();

	SentenceExtractor createSentenceExtractor();

	StopWordFilter createStopWordFilter();

	Tagger createTagger();

	/**
	 * Creates a concrete factory according to the language of the given file.
	 *
	 * @param textFile a text file
	 * @return a concrete factory
	 */
	static PreprocessingFactory from(final File textFile) {
		final var locale = new JLanILanguageExtractor().extract(textFile);
		switch (locale.getLanguage()) {
			case "de":
				return new DEFactory();
			case "en":
				return new ENFactory();
			default:
				throw new IllegalArgumentException("Unsupported locale: " + locale);
		}
	}

}

package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.factory;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.AbbreviationRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.BaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.CaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.NounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.PhraseDetector;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.SentenceCleaner;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.SentenceExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.StopWordRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Tagger;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.JLanILanguageExtractor;

import java.nio.file.Path;

/**
 * Defines a factory for the components used in preprocessing (Abstract Factory Pattern).
 *
 * @author Nils Wende
 */
public interface PreprocessingFactory {

	AbbreviationRemover createAbbreviationRemover();

	BaseFormReducer createBaseFormReducer();

	CaseNormalizer createCaseNormalizer();

	SentenceCleaner createSentenceCleaner();

	NounFilter createNounFilter();

	PhraseDetector createPhraseDetector();

	SentenceExtractor createSentenceExtractor();

	StopWordRemover createStopWordRemover();

	Tagger createTagger();

	/**
	 * Creates a concrete factory according to the language of the given file.
	 *
	 * @param textFile a text file
	 * @return a concrete factory
	 */
	static PreprocessingFactory from(final Path textFile) {
		final var languageExtractor = new JLanILanguageExtractor();
		final var locale = languageExtractor.extract(textFile);
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

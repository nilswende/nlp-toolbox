package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.factory;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.AbbreviationRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.BaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.CaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.NounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.SentenceCleaner;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.StopWordRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Tagger;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.JLanILanguageExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.phrases.PhraseDetector;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
		final var language = locale.getLanguage();
		return Cache.get(language);
	}

	/**
	 * Creates a concrete factory according to the given language.
	 *
	 * @param language a {@link java.util.Locale} language code
	 * @return a concrete factory
	 */
	static PreprocessingFactory from(String language) {
		switch (language) {
			case "de":
				return new DEFactory();
			case "en":
				return new ENFactory();
			default:
				throw new IllegalArgumentException("Unsupported language: " + language);
		}
	}

	/**
	 * Caches the created {@link PreprocessingFactory}s.<br>
	 * This high-level optimization is only possible because the preprocessing is entirely single threaded.
	 *
	 * @author Nils Wende
	 */
	class Cache {
		private static final Map<String, PreprocessingFactory> cache = new HashMap<>();

		/**
		 * Returns a concrete factory according to the given language.
		 *
		 * @param language a language
		 * @return a concrete factory
		 */
		static PreprocessingFactory get(final String language) {
			return cache.computeIfAbsent(language, PreprocessingFactory::from);
		}
	}

}

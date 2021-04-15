package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.factory;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.AbbreviationRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.BaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.CaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.NounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.PhraseExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.SentenceCleaner;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.SentenceExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.StopWordRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Tagger;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Tagset;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.ASVStopWordRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.DEBaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.DECaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.FileAbbreviationRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.IndexerPhraseExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.RegexSentenceCleaner;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.TaggedNounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.ViterbiTagger;
import de.fernuni_hagen.kn.nlp.preprocessing.textual.SimpleSentenceExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.textual.impl.Text2SatzWhitespaceRemover;
import te.utils.Parameters;

import java.util.Locale;

/**
 * Concrete factory for the preprocessing of german text.
 *
 * @author Nils Wende
 */
public class DEFactory implements PreprocessingFactory {

	private static final Locale LOCALE = Locale.GERMAN;
	private static final int ASV_LANGUAGE = Parameters.DE;

	@Override
	public AbbreviationRemover createAbbreviationRemover() {
		return new FileAbbreviationRemover("abbreviations/abbrev.txt");
	}

	@Override
	public BaseFormReducer createBaseFormReducer() {
		return new DEBaseFormReducer();
	}

	@Override
	public CaseNormalizer createCaseNormalizer() {
		return new DECaseNormalizer();
	}

	@Override
	public SentenceCleaner createSentenceCleaner() {
		return new RegexSentenceCleaner();
	}

	@Override
	public NounFilter createNounFilter() {
		return new TaggedNounFilter();
	}

	@Override
	public PhraseExtractor createPhraseExtractor() {
		return new IndexerPhraseExtractor(ASV_LANGUAGE);
	}

	@Override
	public SentenceExtractor createSentenceExtractor() {
		return new SimpleSentenceExtractor(LOCALE, new Text2SatzWhitespaceRemover());
	}

	@Override
	public StopWordRemover createStopWordRemover() {
		return new ASVStopWordRemover(LOCALE, ASV_LANGUAGE);
	}

	@Override
	public Tagger createTagger() {
		return new ViterbiTagger(Tagset.STTS);
	}

}

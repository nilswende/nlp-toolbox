package de.fernuni_hagen.kn.nlp.preprocessing.factory;

import de.fernuni_hagen.kn.nlp.input.SimpleSentenceExtractor;
import de.fernuni_hagen.kn.nlp.input.impl.RegexWhitespaceRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.AbbreviationRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.BaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.CaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.NounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.PhraseExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.SentenceCleaner;
import de.fernuni_hagen.kn.nlp.preprocessing.SentenceExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.StopWordRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.Tagger;
import de.fernuni_hagen.kn.nlp.preprocessing.Tagset;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.ASVStopWordRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.DEBaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.DECaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.FileAbbreviationRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.IndexerPhraseExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.RegexSentenceCleaner;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.TaggedNounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.ViterbiTagger;
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
		return new SimpleSentenceExtractor(LOCALE, new RegexWhitespaceRemover());
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

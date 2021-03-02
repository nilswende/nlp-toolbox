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
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.ENBaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.ENCaseNormalizer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.IndexerPhraseExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.RegexSentenceCleaner;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.TaggedNounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl.ViterbiTagger;
import de.fernuni_hagen.kn.nlp.preprocessing.textual.SimpleSentenceExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.textual.impl.RegexWhitespaceRemover;
import org.apache.commons.lang3.NotImplementedException;
import te.utils.Parameters;

import java.util.Locale;

/**
 * Concrete factory for the preprocessing of english text.
 *
 * @author Nils Wende
 */
public class ENFactory implements PreprocessingFactory {

	private static final Locale LOCALE = Locale.ENGLISH;
	private static final int ASV_LANGUAGE = Parameters.EN;

	@Override
	public AbbreviationRemover createAbbreviationRemover() {
		throw new NotImplementedException("no AbbreviationRemover implemented for locale " + LOCALE);
	}

	@Override
	public BaseFormReducer createBaseFormReducer() {
		return new ENBaseFormReducer();
	}

	@Override
	public CaseNormalizer createCaseNormalizer() {
		return new ENCaseNormalizer();
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
		return new ViterbiTagger(Tagset.BNC);
	}

}

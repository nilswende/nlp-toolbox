package de.fernuni_hagen.kn.nlp.preprocessing.factory;

import de.fernuni_hagen.kn.nlp.input.SimpleSentenceExtractor;
import de.fernuni_hagen.kn.nlp.input.impl.RegexWhitespaceRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.AbbreviationFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.BaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.NounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.PhraseExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.SentenceExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.StopWordFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.Tagger;
import de.fernuni_hagen.kn.nlp.preprocessing.Tagset;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.ASVStopWordFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.DEBaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.FileAbbreviationFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.IndexerPhraseExtractor;
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
	public AbbreviationFilter createAbbreviationFilter() {
		return new FileAbbreviationFilter("abbreviations/abbrev.txt");
	}

	@Override
	public BaseFormReducer createBaseFormReducer() {
		return new DEBaseFormReducer();
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
	public StopWordFilter createStopWordFilter() {
		return new ASVStopWordFilter(LOCALE, ASV_LANGUAGE);
	}

	@Override
	public Tagger createTagger() {
		return new ViterbiTagger(Tagset.STTS);
	}

}

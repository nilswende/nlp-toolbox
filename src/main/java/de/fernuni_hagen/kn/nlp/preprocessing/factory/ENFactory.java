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
import de.fernuni_hagen.kn.nlp.preprocessing.impl.ENBaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.IndexerPhraseExtractor;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.TaggedNounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.ViterbiTagger;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Locale;

/**
 * Concrete factory for the preprocessing of english text.
 *
 * @author Nils Wende
 */
public class ENFactory implements PreprocessingFactory {

	private static final Locale LOCALE = Locale.ENGLISH;

	private final int asvLanguage;

	public ENFactory(final int asvLanguage) {
		this.asvLanguage = asvLanguage;
	}

	@Override
	public AbbreviationFilter createAbbreviationFilter() {
		throw new NotImplementedException("no AbbreviationFilter implemented for " + LOCALE);
	}

	@Override
	public BaseFormReducer createBaseFormReducer() {
		return new ENBaseFormReducer();
	}

	@Override
	public NounFilter createNounFilter() {
		return new TaggedNounFilter();
	}

	@Override
	public PhraseExtractor createPhraseExtractor() {
		return new IndexerPhraseExtractor(asvLanguage);
	}

	@Override
	public SentenceExtractor createSentenceExtractor() {
		return new SimpleSentenceExtractor(LOCALE, new RegexWhitespaceRemover());
	}

	@Override
	public StopWordFilter createStopWordFilter() {
		return new ASVStopWordFilter(LOCALE, asvLanguage);
	}

	@Override
	public Tagger createTagger() {
		return new ViterbiTagger(Tagset.from(LOCALE));
	}

}

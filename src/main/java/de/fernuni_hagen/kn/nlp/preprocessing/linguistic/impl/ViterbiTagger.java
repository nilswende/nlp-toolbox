package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.FileSaver;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.TaggedTerm;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Tagger;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Tagset;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Uses POS tagging on sentences.
 *
 * @author Nils Wende
 */
public class ViterbiTagger implements Tagger {

	private final Tagset tagset;
	private final de.uni_leipzig.asv.toolbox.viterbitagger.Tagger tagger;
	private final FileSaver fileSaver = new FileSaver("taggedSentences", false);

	/**
	 * Creates a new instance.
	 *
	 * @param tagset the tagset used for tagging
	 */
	public ViterbiTagger(final Tagset tagset) {
		this.tagset = tagset;
		tagger = new de.uni_leipzig.asv.toolbox.viterbitagger.Tagger(
				tagset.getTagList(),
				tagset.getLexicon(),
				tagset.getTransitions(),
				false);
		setExtern();
		tagger.setReplaceNumbers(false);
		tagger.setUseInternalTok(true);
	}

	private void setExtern() { // improve abysmal performance
		final var clazz = tagger.getClass();
		try {
			final var extern = clazz.getDeclaredField("extern");
			extern.setAccessible(true);
			extern.set(tagger, false);
			extern.setAccessible(false);
			tagger.setExtern(false);
		} catch (final NoSuchFieldException | IllegalAccessException e) {
			throw new UncheckedException(e);
		}
	}

	@Override
	public List<TaggedTerm> apply(final String sentence) {
		final var taggedSentence = tagger.tagSentence(sentence).stripLeading();
		fileSaver.println(taggedSentence);
		final var terms = taggedSentence.split(StringUtils.SPACE);
		return Arrays.stream(terms)
				.map(term -> TaggedTerm.from(term, tagset))
				.collect(Collectors.toList());
	}

}

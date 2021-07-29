package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import de.fernuni_hagen.kn.nlp.file.Exporter;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Tagger;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.Tagset;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.data.TaggedTerm;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Uses POS tagging on sentences.
 *
 * @author Nils Wende
 */
public class ViterbiTagger implements Tagger {

	private final Exporter exporter = new Exporter("data/output/taggedSentences.txt", false);
	private final Tagset tagset;
	private final de.uni_leipzig.asv.toolbox.viterbitagger.Tagger tagger;

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
		tagger.setReplaceNumbers(false);
		tagger.setUseInternalTok(true);
	}

	@Override
	public List<TaggedTerm> apply(final String sentence) {
		final var taggedSentence = tagger.tagSentence2(sentence);
		exporter.println(() -> String.join(StringUtils.SPACE, taggedSentence));
		return taggedSentence.stream()
				.map(term -> TaggedTerm.from(term, tagset))
				.collect(Collectors.toList());
	}

}

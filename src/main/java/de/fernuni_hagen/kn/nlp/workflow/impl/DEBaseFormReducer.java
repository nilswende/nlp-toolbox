package de.fernuni_hagen.kn.nlp.workflow.impl;

import de.fernuni_hagen.kn.nlp.workflow.BaseFormReducer;
import de.fernuni_hagen.kn.nlp.workflow.TaggedWord;
import de.uni_leipzig.asv.toolbox.baseforms.Zerleger2;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Reduces the terms of a german sentence to their base forms.
 *
 * @author Nils Wende
 */
public class DEBaseFormReducer implements BaseFormReducer {

	private final Zerleger2 reducer;

	public DEBaseFormReducer() {
		reducer = new Zerleger2();
		final var path = Path.of("resources", "trees");
		reducer.init(path.resolve("kompVVic.tree").toString(),
				path.resolve("kompVHic.tree").toString(),
				path.resolve("grfExt.tree").toString());
	}

	@Override
	public Stream<TaggedWord> apply(final Stream<TaggedWord> sentence) {
		return sentence.map(w -> new TaggedWord(reducer.grundFormReduktion(w.getTerm()), w));
	}

}

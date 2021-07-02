package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.BaseFormReducer;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.data.TaggedTerm;
import de.uni_leipzig.asv.toolbox.baseforms.Zerleger2;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Reduces the terms of a german sentence to their base forms.
 *
 * @author Nils Wende
 */
public class DEBaseFormReducer implements BaseFormReducer {

	private final Zerleger2 reducer = new Zerleger2();

	{
		final var path = Path.of("resources", "trees");
		reducer.init(path.resolve("kompVVic.tree").toString(),
				path.resolve("kompVHic.tree").toString(),
				path.resolve("grfExt.tree").toString());
	}

	@Override
	public Stream<TaggedTerm> apply(final Stream<TaggedTerm> sentence) {
		return sentence.map(this::transform);
	}

	private TaggedTerm transform(final TaggedTerm term) {
		return term.isProperNoun() ? term : term.withTerm(reducer::grundFormReduktion);
	}

}

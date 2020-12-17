package de.fernuni_hagen.kn.nlp.workflow;

import java.util.stream.Stream;

/**
 * A variable step in the preprocessing workflow.
 *
 * @author Nils Wende
 */
interface WorkflowStep {

	/**
	 * Applies the step to the given sentence.
	 *
	 * @param sentence the words of a sentence
	 * @return the sentence with this workflow step applied
	 */
	Stream<TaggedWord> apply(Stream<TaggedWord> sentence);

}

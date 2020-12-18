package de.fernuni_hagen.kn.nlp.preprocessing;

import de.fernuni_hagen.kn.nlp.config.Config;
import de.fernuni_hagen.kn.nlp.input.SimpleSentenceExtractor;
import de.fernuni_hagen.kn.nlp.input.impl.JLanILanguageExtractor;
import de.fernuni_hagen.kn.nlp.input.impl.RegexWhitespaceRemover;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.ASVStopWordFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.FileAbbreviationFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.TaggedNounFilter;
import de.fernuni_hagen.kn.nlp.preprocessing.impl.ViterbiTagger;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Executes the linguistic preprocessing of a document.
 *
 * @author Nils Wende
 */
public class Preprocessor {

	private final List<Function<Locale, PreprocessingStep>> workflowSteps;

	protected Preprocessor(final List<Function<Locale, PreprocessingStep>> workflowSteps) {
		this.workflowSteps = workflowSteps;
	}

	/**
	 * Executes the linguistic preprocessing of a document.
	 *
	 * @param document the document to be processed
	 * @return stream of the sentences inside the document, split into words
	 */
	public Stream<List<String>> preprocess(final File document) {
		final var locale = new JLanILanguageExtractor().extract(document);
		final var sentenceExtractor = new SimpleSentenceExtractor(locale, new RegexWhitespaceRemover());
		// file level
		final var sentences = sentenceExtractor.extract(document).collect(Collectors.toList());
		return processSentences(sentences.stream(), locale);
	}

	protected Stream<List<String>> processSentences(final Stream<String> sentences, final Locale locale) {
		if (workflowSteps.isEmpty()) {
			return simpleProcessing(sentences);
		}
		return applyWorkflowSteps(createTaggedStream(sentences, locale), locale)
				.map(s -> s.map(TaggedWord::getTerm))
				.map(s -> s.collect(Collectors.toList()));
	}

	private Stream<List<String>> simpleProcessing(final Stream<String> sentences) {
		return sentences
				.map(s -> s.split(StringUtils.SPACE))
				.map(Arrays::stream)
				.map(s -> s.collect(Collectors.toList()));
	}

	private Stream<Stream<TaggedWord>> createTaggedStream(final Stream<String> sentences, final Locale locale) {
		final var tagger = new ViterbiTagger(locale);
		return sentences.map(tagger::tag);
	}

	private Stream<Stream<TaggedWord>> applyWorkflowSteps(Stream<Stream<TaggedWord>> stream, final Locale locale) {
		final var steps = workflowSteps.stream().map(step -> step.apply(locale)).collect(Collectors.toList());
		for (final PreprocessingStep step : steps) {
			stream = stream.map(step::apply);
		}
		return stream;
	}

	/**
	 * Creates a new preprocessor from the given config.
	 *
	 * @param config Config
	 * @return a new preprocessor
	 */
	public static Preprocessor from(final Config config) {
		final var steps = new ArrayList<Function<Locale, PreprocessingStep>>();
		if (config.useBaseFormReduction()) {
			steps.add(BaseFormReducer::from);
		}
		if (config.filterNouns()) {
			steps.add(l -> new TaggedNounFilter());
		}
		if (config.removeStopWords()) {
			steps.add(ASVStopWordFilter::new);
		}
		if (config.removeAbbreviations()) {
			steps.add(FileAbbreviationFilter::new);
		}
		return config.extractPhrases() ? new PhrasePreprocessor(steps) : new Preprocessor(steps);
	}

}

package de.fernuni_hagen.kn.nlp.config;

/**
 * Contains the preprocessing config.
 *
 * @author Nils Wende
 */
public class PreprocessingConfig extends UseCaseConfig {

	private boolean keepTempFiles;
	private int sentenceFileSizeLimitBytes;
	private boolean extractPhrases;
	private boolean useBaseFormReduction;
	private boolean filterNouns;
	private boolean removeStopWords;
	private boolean removeAbbreviations;
	private boolean normalizeCase;

	public boolean keepTempFiles() {
		return keepTempFiles;
	}

	public int getSentenceFileSizeLimitBytes() {
		return sentenceFileSizeLimitBytes <= 0 ? Integer.MAX_VALUE : sentenceFileSizeLimitBytes;
	}

	public boolean extractPhrases() {
		return extractPhrases;
	}

	public boolean useBaseFormReduction() {
		return useBaseFormReduction;
	}

	public boolean filterNouns() {
		return filterNouns;
	}

	public boolean removeStopWords() {
		return removeStopWords;
	}

	public boolean removeAbbreviations() {
		return removeAbbreviations;
	}

	public boolean normalizeCase() {
		return normalizeCase;
	}
}

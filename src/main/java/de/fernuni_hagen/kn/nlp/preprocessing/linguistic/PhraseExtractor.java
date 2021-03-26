package de.fernuni_hagen.kn.nlp.preprocessing.linguistic;

import java.util.List;
import java.util.stream.Stream;

/**
 * Extracts phrases from a text.
 *
 * @author Nils Wende
 */
public interface PhraseExtractor {

	/**
	 * Extracts phrases from a text.
	 * The phrases are removed from the text's sentences and put in a different list.
	 *
	 * @param sentences the text from which phrases should be extracted
	 * @return the extracted values
	 */
	Stream<Extraction> extractPhrases(Stream<String> sentences);

	class Extraction {
		private final String sentence;
		private final String originalSentence;
		private final List<String> phrases;

		public Extraction(final String sentence, final String originalSentence, final List<String> phrases) {
			this.sentence = sentence;
			this.originalSentence = originalSentence;
			this.phrases = phrases;
		}

		public String getSentence() {
			return sentence;
		}

		public String getOriginalSentence() {
			return originalSentence;
		}

		public List<String> getPhrases() {
			return phrases;
		}
	}

}

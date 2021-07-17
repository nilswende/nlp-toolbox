package de.fernuni_hagen.kn.nlp.preprocessing.linguistic.impl;

import JLanI.kernel.DataSourceException;
import JLanI.kernel.LanIKernel;
import JLanI.kernel.Request;
import JLanI.kernel.RequestException;
import JLanI.kernel.Response;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.preprocessing.linguistic.factory.LanguageExtractor;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import static de.fernuni_hagen.kn.nlp.preprocessing.linguistic.PreprocessingUtils.cast;

/**
 * Extracts the language from a text file.
 *
 * @author Nils Wende
 */
public class JLanILanguageExtractor implements LanguageExtractor {

	@Override
	public Locale extract(final Path textFile) {
		try (final var reader = FileHelper.newFileReader(textFile)) {
			return extract(reader, getInputLength(textFile));
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private int getInputLength(final Path textFile) throws IOException {
		return (int) Math.max(Math.sqrt(Files.size(textFile)), 30);
	}

	Locale extract(final Reader reader, final int inputLength) {
		final var testString = getTestString(reader, inputLength);
		final var response = evaluate(testString);
		return getLocale(response);
	}

	private String getTestString(final Reader reader, final int inputLength) {
		try {
			final var chars = new char[inputLength];
			final var read = IOUtils.read(reader, chars);
			return String.valueOf(chars, 0, read);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private Response evaluate(final String testString) {
		try {
			final var request = new Request();
			request.setSentence(testString);
			request.setReduce(true);
			return LanIKernel.getInstance().evaluate(request);
		} catch (final DataSourceException | RequestException e) {
			throw new UncheckedException(e);
		}
	}

	private Locale getLocale(final Response response) {
		final Map<String, Double> result = cast(response.getResult());
		final var language = result.entrySet().stream()
				.max(Map.Entry.comparingByValue())
				.orElseGet(() -> Map.entry("en", 0.0))
				.getKey();
		return new Locale(language);
	}

}

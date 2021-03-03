package de.fernuni_hagen.kn.nlp.config;

import com.google.gson.Gson;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses configs from JSON command-line arguments.
 *
 * @author Nils Wende
 */
public class JsonConfigParser extends ConfigParser {

	private static final Gson GSON = new Gson();
	private static final Pattern NAME_PATTERN = Pattern.compile("name\\s*:\\s*(\\w+)");

	/**
	 * Parses the specified configs from the given JSON command-line arguments.
	 *
	 * @param args JSON command-line arguments
	 */
	public JsonConfigParser(final String[] args) {
		super(args);
	}

	@Override
	protected AppConfig createAppConfig(final String appValue) {
		if (StringUtils.isEmpty(appValue)) {
			return new AppConfig();
		} else {
			return GSON.fromJson(getAppJson(appValue), AppConfig.class);
		}
	}

	private String getAppJson(final String appValue) {
		if (isFile(appValue)) {
			final var path = Path.of(appValue);
			if (Files.exists(path)) {
				return getJson(path);
			}
			throw new IllegalArgumentException("File " + path + " doesn't exist.");
		} else if (isJson(appValue)) {
			return appValue;
		}
		throw new IllegalArgumentException("AppConfig can't be created from " + appValue);
	}

	@Override
	protected List<UseCaseConfig> createUseCaseConfigs(final List<String> useCaseValues) {
		final var allJson = useCaseValues.stream()
				.map(this::getJson)
				.collect(Collectors.toList());
		System.out.println(allJson);
		return getUseCases(allJson);
	}

	private String getJson(final String arg) {
		if (isFile(arg)) {
			final var path = Path.of(arg);
			if (Files.exists(path)) {
				return getJson(path);
			}
			throw new IllegalArgumentException("File " + path + " doesn't exist.");
		} else if (isJson(arg)) {
			return arg;
		} else {
			return "{name:" + arg + "}";
		}
	}

	private boolean isFile(final String arg) {
		return arg.endsWith(".json");
	}

	private String getJson(final Path path) {
		try (final var reader = FileHelper.newFileReader(path)) {
			return IOUtils.toString(reader);
		} catch (final IOException e) {
			throw new UncheckedException(e);
		}
	}

	private boolean isJson(final String arg) {
		return arg.endsWith("}");
	}

	private List<UseCaseConfig> getUseCases(final List<String> args) {
		final var useCases = new ArrayList<UseCaseConfig>();
		for (final String arg : args) {
			final var matcher = NAME_PATTERN.matcher(arg);
			if (matcher.find()) {
				final var name = matcher.group(1);
				final var useCase = UseCases.fromIgnoreCase(name);
				final var config = GSON.fromJson(arg, useCase.getConfigClass());
				useCases.add(config);
			} else {
				throw new IllegalArgumentException(arg + " contains no use case name.");
			}
		}
		return useCases;
	}

}

package de.fernuni_hagen.kn.nlp.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.fernuni_hagen.kn.nlp.file.FileHelper;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import de.fernuni_hagen.kn.nlp.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parses configs from JSON command-line arguments.
 *
 * @author Nils Wende
 */
public class JsonConfigParser extends ConfigParser {

	private static final Gson GSON = new Gson();
	private static final Pattern NAME_PATTERN = Pattern.compile("name\"?\\s*:\\s*\"?(\\w+)");

	/**
	 * Parses the specified configs from the given JSON command-line arguments.
	 *
	 * @param args JSON command-line arguments
	 */
	public JsonConfigParser(final String[] args) {
		super(args);
	}

	@Override
	protected AppConfig createAppConfig(final List<String> appValue) {
		final var strings = String.join(StringUtils.EMPTY, joinJsonParts(appValue));
		System.out.println(strings);
		return GSON.fromJson(getAppJson(strings), AppConfig.class);
	}

	private String getAppJson(final String appValue) {
		if (isFile(appValue)) {
			final var path = Path.of(appValue);
			if (Files.exists(path)) {
				return getJson(path);
			}
			throw new IllegalArgumentException("File '" + path + "' doesn't exist.");
		} else if (isJson(appValue)) {
			return appValue;
		}
		throw new IllegalArgumentException("AppConfig can't be created from '" + appValue + "'");
	}

	@Override
	protected List<UseCaseConfig> createUseCaseConfigs(final List<String> useCaseValues) {
		return joinJsonParts(useCaseValues).stream()
				.map(this::getJson)
				.map(this::asJsonArray)
				.peek(System.out::println)
				.flatMap(this::getUseCaseConfigs)
				.collect(Collectors.toList());
	}

	private List<String> joinJsonParts(final List<String> useCaseValues) {
		final var list = new ArrayList<String>();
		var sb = new StringBuilder();
		for (final String value : useCaseValues) {
			if (value.contains("{") || sb.length() > 0) {
				sb.append(value);
			} else {
				list.add(value);
			}
			if (value.contains("}")) {
				list.add(sb.toString());
				sb = new StringBuilder();
			}
		}
		return list;
	}

	private String getJson(final String arg) {
		if (isFile(arg)) {
			final var path = Path.of(arg);
			if (Files.exists(path)) {
				return getJson(path);
			}
			throw new IllegalArgumentException("File '" + path + "' doesn't exist.");
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

	private String asJsonArray(final String json) {
		return json.stripLeading().startsWith("[") ? json : "[" + json + "]";
	}

	private Stream<UseCaseConfig> getUseCaseConfigs(final String arg) {
		final var jsonArray = JsonParser.parseString(arg).getAsJsonArray();
		return Utils.stream(jsonArray)
				.map(JsonElement::toString)
				.map(this::getUseCaseConfig);
	}

	private UseCaseConfig getUseCaseConfig(final String arg) {
		final var matcher = NAME_PATTERN.matcher(arg);
		if (matcher.find()) {
			final var name = matcher.group(1);
			final var useCase = UseCases.fromIgnoreCase(name);
			return GSON.fromJson(arg, useCase.getConfigClass());
		} else {
			throw new IllegalArgumentException("'" + arg + "' contains no use case name.");
		}
	}

}

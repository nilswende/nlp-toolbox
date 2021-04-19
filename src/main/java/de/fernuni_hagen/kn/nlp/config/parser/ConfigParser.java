package de.fernuni_hagen.kn.nlp.config.parser;

import de.fernuni_hagen.kn.nlp.UseCase;
import de.fernuni_hagen.kn.nlp.config.AppConfig;
import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.List;

/**
 * Parses configs from command-line arguments.
 *
 * @author Nils Wende
 */
public abstract class ConfigParser {

	/**
	 * The parameter name for the app config.
	 */
	protected static final String APP_OPT = "a";
	/**
	 * The parameter name for the use case configs.
	 */
	protected static final String USE_CASE_OPT = "u";

	/**
	 * The parsed app config.
	 */
	protected final AppConfig appConfig;
	/**
	 * The parsed use case configs.
	 */
	protected final List<UseCase> useCases;

	/**
	 * Parses the specified configs from the given command-line arguments.
	 *
	 * @param args command-line arguments
	 */
	protected ConfigParser(final String[] args) {
		final var options = new Options();
		options.addOption(Option.builder(APP_OPT).longOpt("appConfig").desc("path to the config file").hasArgs().build());
		options.addOption(Option.builder(USE_CASE_OPT).longOpt("useCaseConfigs").hasArgs().required().build());
		try {
			final var parser = new DefaultParser();
			final var cli = parser.parse(options, args);
			appConfig = readAppConfig(cli);
			final var useCaseValues = List.of(cli.getOptionValues(USE_CASE_OPT));
			useCases = parseUseCases(useCaseValues);
		} catch (final ParseException e) {
			throw new UncheckedException(e);
		}
	}

	private AppConfig readAppConfig(final CommandLine cli) {
		if (cli.hasOption(APP_OPT)) {
			final var appValue = List.of(cli.getOptionValues(APP_OPT));
			return parseAppConfig(appValue);
		} else {
			return new AppConfig();
		}
	}

	/**
	 * Parses the app config.
	 *
	 * @param appValue the specified app config strings, may be null
	 * @return the parsed app config
	 */
	protected abstract AppConfig parseAppConfig(List<String> appValue);

	/**
	 * Parses the use case configs.
	 *
	 * @param useCaseValues the specified, non-null use case strings
	 * @return the parsed use case configs
	 */
	protected abstract List<UseCase> parseUseCases(List<String> useCaseValues);

	/**
	 * Returns the parsed app config.
	 *
	 * @return the parsed app config
	 */
	public AppConfig getAppConfig() {
		return appConfig;
	}

	/**
	 * Returns the parsed use case configs.
	 *
	 * @return the parsed use case configs
	 */
	public List<UseCase> getUseCases() {
		return useCases;
	}

}

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

	protected static final String APP_OPT = "a";
	protected static final String USE_CASE_OPT = "u";

	protected final AppConfig appConfig;
	protected final List<UseCase> useCases;

	/**
	 * Parses the specified configs from the given command-line arguments.
	 *
	 * @param args command-line arguments
	 */
	protected ConfigParser(final String[] args) {
		final var options = new Options();
		options.addOption(Option.builder(APP_OPT).longOpt("appConfig").desc("path to the config file, default: " + AppConfig.getDefaultConfigFilePath()).hasArgs().build());
		options.addOption(Option.builder(USE_CASE_OPT).longOpt("useCaseConfigs").hasArgs().required().build());
		try {
			final var parser = new DefaultParser();
			final var cli = parser.parse(options, args);
			appConfig = readAppConfig(cli);
			final var useCaseValues = List.of(cli.getOptionValues(USE_CASE_OPT));
			useCases = createUseCases(useCaseValues);
		} catch (final ParseException e) {
			throw new UncheckedException(e);
		}
	}

	private AppConfig readAppConfig(final CommandLine cli) {
		if (cli.hasOption(APP_OPT)) {
			final var appValue = List.of(cli.getOptionValues(APP_OPT));
			return createAppConfig(appValue);
		} else {
			return new AppConfig();
		}
	}

	/**
	 * Returns the parsed app config.
	 *
	 * @param appValue the specified app config, may be null
	 * @return the parsed app config
	 */
	protected abstract AppConfig createAppConfig(List<String> appValue);

	/**
	 * Returns all parsed use case configs.
	 *
	 * @param useCaseValues the specified, non-null use cases
	 * @return all parsed use case configs
	 */
	protected abstract List<UseCase> createUseCases(List<String> useCaseValues);

	public AppConfig getAppConfig() {
		return appConfig;
	}

	public List<UseCase> getUseCases() {
		return useCases;
	}

}

package de.fernuni_hagen.kn.nlp.config;

import de.fernuni_hagen.kn.nlp.utils.UncheckedException;
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
	protected final List<UseCaseConfig> useCaseConfigs;

	/**
	 * Parses the specified configs from the given command-line arguments.
	 *
	 * @param args command-line arguments
	 */
	protected ConfigParser(final String[] args) {
		final var options = new Options();
		options.addOption(Option.builder(APP_OPT).longOpt("appConfig").desc("path to the config file, default: " + AppConfig.getDefaultConfigFilePath()).hasArg().build());
		options.addOption(Option.builder(USE_CASE_OPT).longOpt("useCaseConfigs").hasArgs().required().build());
		try {
			final var parser = new DefaultParser();
			final var cli = parser.parse(options, args);
			final var appValue = cli.getOptionValue(APP_OPT);
			final var useCaseValues = List.of(cli.getOptionValues(USE_CASE_OPT));
			appConfig = createAppConfig(appValue);
			useCaseConfigs = createUseCaseConfigs(useCaseValues);
			System.out.println(useCaseConfigs);
		} catch (final ParseException e) {
			throw new UncheckedException(e);
		}
	}

	/**
	 * Returns the parsed app config.
	 *
	 * @param appValue the specified app config, may be null
	 * @return the parsed app config
	 */
	protected abstract AppConfig createAppConfig(String appValue);

	/**
	 * Returns all parsed use case configs.
	 *
	 * @param useCaseValues the specified, non-null use cases
	 * @return all parsed use case configs
	 */
	protected abstract List<UseCaseConfig> createUseCaseConfigs(List<String> useCaseValues);

	public AppConfig getAppConfig() {
		return appConfig;
	}

	public List<UseCaseConfig> getUseCaseConfigs() {
		return useCaseConfigs;
	}

}

package de.fernuni_hagen.kn.nlp.config;

import java.util.List;

/**
 * Contains the boolean retrieval config.
 *
 * @author Nils Wende
 */
public class BooleanRetrievalConfig extends UseCaseConfig {

	private List<String> query;

	public List<String> getQuery() {
		return query;
	}
}

package de.fernuni_hagen.kn.nlp.analysis;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.Map;
import java.util.Set;

/**
 * Uses the HITS algorithm to find hubs and authorities in a directed graph.
 *
 * @author Nils Wende
 */
class DirectedHITS extends HITS {

	public DirectedHITS(final Config config) {
		super(config);
	}

	/**
	 * Uses the HITS algorithm to find hubs and authorities in a directed graph.
	 *
	 * @param db DB
	 * @return HITS scores
	 */
	@Override
	public Map<String, Scores> calculate(final DBReader db) {
		final var hub2auths = db.getSignificances(config.getWeightingFunction());
		final var auth2hubs = Maps.invertMapping(hub2auths);
		return getStringScoresMap(auth2hubs, hub2auths);
	}

	@Override
	protected Set<String> getTerms(final Map<String, Map<String, Double>> linking) {
		return Maps.getKeys(linking);
	}

}

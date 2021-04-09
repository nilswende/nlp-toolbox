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
public class DirectedHITS extends HITS {

	@Override
	public void execute(final DBReader dbReader) {
		final var hub2auths = dbReader.getDirectedSignificances(weightingFunction);
		final var auth2hubs = Maps.invertMapping(hub2auths);
		calcScores(auth2hubs, hub2auths);
	}

	@Override
	protected Set<String> getTerms(final Map<String, Map<String, Double>> linking) {
		return Maps.getKeys(linking);
	}

}

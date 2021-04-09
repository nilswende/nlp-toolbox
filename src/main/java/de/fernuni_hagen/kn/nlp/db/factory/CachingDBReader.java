package de.fernuni_hagen.kn.nlp.db.factory;

import de.fernuni_hagen.kn.nlp.DBReader;
import de.fernuni_hagen.kn.nlp.graph.WeightedPath;
import de.fernuni_hagen.kn.nlp.math.WeightingFunction;
import de.fernuni_hagen.kn.nlp.utils.Maps;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Caches the return values of a DBReader.
 *
 * @author Nils Wende
 */
class CachingDBReader implements DBReader {

	private final DBReader dbReader;

	private Map<String, Map<String, Double>> cooccurrences;
	private final Map<WeightingFunction, Map<String, Map<String, Double>>> significances = new EnumMap<>(WeightingFunction.class);
	private final Map<WeightingFunction, Map<String, Map<String, Double>>> directedSignificances = new EnumMap<>(WeightingFunction.class);
	private Map<String, Map<String, Long>> termFrequencies;

	CachingDBReader(final DBReader dbReader) {
		this.dbReader = dbReader;
	}

	// since the values returned by a DBReader can be mutable, we need to return a copy

	@Override
	public Map<String, Map<String, Double>> getCooccurrences() {
		if (cooccurrences == null) {
			cooccurrences = dbReader.getCooccurrences();
		}
		return Maps.copyOf(cooccurrences);
	}

	@Override
	public Map<String, Map<String, Double>> getSignificances(final WeightingFunction function) {
		return Maps.copyOf(significances.computeIfAbsent(function, dbReader::getSignificances));
	}

	@Override
	public Map<String, Map<String, Double>> getDirectedSignificances(final WeightingFunction function) {
		return Maps.copyOf(directedSignificances.computeIfAbsent(function, dbReader::getDirectedSignificances));
	}

	@Override
	public Map<String, Map<String, Long>> getTermFrequencies() {
		if (termFrequencies == null) {
			termFrequencies = dbReader.getTermFrequencies();
		}
		return Maps.copyOf(termFrequencies);
	}

	@Override
	public WeightedPath getShortestPath(final String start, final String end, final WeightingFunction function) {
		return dbReader.getShortestPath(start, end, function);
	}

	@Override
	public List<List<String>> getAllSentencesInDocument(final String name) {
		return dbReader.getAllSentencesInDocument(name);
	}
}

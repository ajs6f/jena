package org.apache.jena.sparql.core.mem;

import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.DatasetPrefixStorage;

public class DatasetPrefixStorageInMemory implements DatasetPrefixStorage {

	private Map<String, PrefixMapping> prefixMappings = new HashMap<>();

	@Override
	public void close() {
		prefixMappings = null;
	}

	@Override
	public void sync() {
		// NO OP
	}

	@Override
	public Set<String> graphNames() {
		return prefixMappings.keySet();
	}

	@Override
	public String readPrefix(final String graphName, final String prefix) {
		return getPrefixMapping(graphName).getNsPrefixURI(prefix);
	}

	@Override
	public String readByURI(final String graphName, final String uriStr) {
		return getPrefixMapping(graphName).getNsURIPrefix(uriStr);
	}

	@Override
	public Map<String, String> readPrefixMap(final String graphName) {
		return getPrefixMapping(graphName).getNsPrefixMap();
	}

	@Override
	public void insertPrefix(final String graphName, final String prefix, final String uri) {
		getPrefixMapping(graphName).setNsPrefix(prefix, uri);
	}

	@Override
	public void loadPrefixMapping(final String graphName, final PrefixMapping pmap) {
		getPrefixMapping(graphName).setNsPrefixes(pmap);
	}

	@Override
	public void removeFromPrefixMap(final String graphName, final String prefix) {
		getPrefixMapping(graphName).removeNsPrefix(prefix);
	}

	@Override
	public PrefixMapping getPrefixMapping() {
		return getPrefixMapping(defaultGraphIRI.getURI());
	}

	@Override
	public PrefixMapping getPrefixMapping(final String graphName) {
		return prefixMappings.computeIfAbsent(graphName, x -> new PrefixMappingImpl());
	}
}

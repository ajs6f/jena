package org.apache.jena.sparql.core.mem;

import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetPrefixStorage;
import org.apache.jena.sparql.core.GraphView;

public class GraphInMemory extends GraphView {

	private final DatasetGraphInMemory datasetGraph;

	protected GraphInMemory(final DatasetGraphInMemory dsg, final Node gn) {
		super(dsg, gn);
		this.datasetGraph = dsg;
	}

	@Override
	protected PrefixMapping createPrefixMapping() {
		final DatasetPrefixStorage prefixes = datasetGraph().getPrefixes();
		return isDefaultGraph() || isUnionGraph() ? prefixes.getPrefixMapping() :
			prefixes.getPrefixMapping(getGraphName().getURI());
	}

	private DatasetGraphInMemory datasetGraph() {
		return datasetGraph;
	}
}

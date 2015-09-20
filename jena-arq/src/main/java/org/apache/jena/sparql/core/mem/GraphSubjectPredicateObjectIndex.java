package org.apache.jena.sparql.core.mem;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

public class GraphSubjectPredicateObjectIndex extends Index {

	@Override
	public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o, final boolean defaultGraph) {
		return _find(g, s, p, o);
	}

	@Override
	public void add(final Quad q) {
		add(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
	}

	@Override
	public void delete(final Quad q) {
		delete(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
	}
}

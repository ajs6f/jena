package org.apache.jena.sparql.core.mem;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public interface TripleTable extends TupleTable<Triple> {

	/**
	 * Search the index using a pattern of slots. {@link Node#ANY} or <code>null</code> will work as a wildcard.
	 *
	 * @param s the subject node of the pattern
	 * @param p the predicate node of the pattern
	 * @param o the object node of the pattern
	 * @return an {@link Stream} of matched triples
	 */
	Stream<Triple> find(Node s, Node p, Node o);

	default void clear() {
		find(null, null, null).forEach(this::delete);
	}
}

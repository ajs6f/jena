package org.apache.jena.sparql.core.mem;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Transactional;

public interface TripleTable extends Transactional {

	/**
	 * Search the index using a pattern of slots. {@link Node#ANY} or <code>null</code> will work as a wildcard.
	 *
	 * @param s the subject node of the pattern
	 * @param p the predicate node of the pattern
	 * @param o the object node of the pattern
	 * @return an {@link Stream} of matched triples
	 */
	Stream<Triple> find(Node s, Node p, Node o);

	/**
	 * Add a {@link Triple} to the index
	 *
	 * @param t the quad to add
	 */
	void add(Triple t);

	/**
	 * Remove a {@link Triple} from the index
	 *
	 * @param t the quad to remove
	 */
	void delete(Triple t);

	@Override
	default void begin(final ReadWrite rw) {
		begin();
	}

	@Override
	default void abort() {
		end();
	}

	/**
	 * Begin a transaction.
	 */
	void begin();

	default void clear() {
		find(null, null, null).forEach(this::delete);
	}
}

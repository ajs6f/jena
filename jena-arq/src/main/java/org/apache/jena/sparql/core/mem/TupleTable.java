package org.apache.jena.sparql.core.mem;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Transactional;

public interface TupleTable<TupleType> extends Transactional {

	/**
	 * Add a tuple to the index
	 *
	 * @param t the tuple to add
	 */
	void add(TupleType t);

	/**
	 * Remove a tuple from the index
	 *
	 * @param t the tuple to remove
	 */
	void delete(TupleType t);

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
}

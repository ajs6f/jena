package org.apache.jena.sparql.core.mem;

import static java.lang.ThreadLocal.withInitial;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.query.ReadWrite;
import org.slf4j.Logger;

public abstract class PMapTupleTable<TupleMapType, TupleType> implements TupleTable<TupleType> {

	/**
	 * We use an {@link AtomicReference} to the internal structure that holds our index data to be able to swap
	 * transactional versions of the index data with the shared version atomically.
	 */
	protected abstract AtomicReference<TupleMapType> master();

	private final ThreadLocal<TupleMapType> local = withInitial(() -> master().get());

	protected ThreadLocal<TupleMapType> local() {
		return local;
	}

	private final ThreadLocal<Boolean> isInTransaction = withInitial(() -> false);

	private final String name;

	public PMapTupleTable(final String n) {
		this.name = n;
	}

	protected abstract Logger log();

	protected void debug(final String msg, final Object... values) {
		log().debug(name + ": " + msg, values);
	}

	@Override
	public void begin(final ReadWrite rw) {
		isInTransaction.set(true);
	}

	@Override
	public void end() {
		debug("Abandoning transactional reference.");
		local.remove();
		isInTransaction.set(false);
	}

	@Override
	public void commit() {
		debug("Swapping transactional reference in for shared reference");
		master().set(local.get());
		end();
	}

	@Override
	public boolean isInTransaction() {
		return isInTransaction.get();
	};
}

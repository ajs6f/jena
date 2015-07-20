package org.apache.jena.sparql.core.journaling;

import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.query.ReadWrite.WRITE;

import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWithLock;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.journaling.QuadOperation.QuadAddition;
import org.apache.jena.sparql.core.journaling.QuadOperation.QuadDeletion;

/**
 * A {@link DatasetGraph} implementation with two key affordances. First, this class keeps a record of operations
 * conducted against it. This enables the implementation of {@link #abort()}, by reversing that record and running it
 * backwards. Second, this class has "copy-on-add" semantics for {@link #addGraph(Node, Graph)}. This means that the
 * transactional semantics of a given {@link Graph} are discarded on add and replaced with those of this class, so that
 * transactional semantics are uniform and therefore useful.
 */
public class DatasetGraphWithRecord extends DatasetGraphWithLock {

	/**
	 * A record of operations for use in rewinding transactions.
	 */
	private final ReversableOperationRecord<QuadOperation> record;

	/**
	 * Indicates whether an transaction abort is in progress.
	 */
	private final ThreadLocal<Boolean> aborting = new ThreadLocal<Boolean>() {

		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	private boolean isAborting() {
		return aborting.get();
	}

	private void startAborting() {
		aborting.set(true);
	}

	private void stopAborting() {
		aborting.set(false);
	}

	public DatasetGraphWithRecord(final DatasetGraph dsg, final ReversableOperationRecord<QuadOperation> record) {
		super(dsg);
		this.record = record;
	}

	@Override
	public void addGraph(final Node graphName, final Graph graph) {
		if (!graph.isEmpty()) graph.find(ANY, ANY, ANY).forEachRemaining(t -> add(new Quad(graphName, t)));
	}

	@Override
	public void removeGraph(final Node graphName) {
		deleteAny(graphName, ANY, ANY, ANY);
		super.removeGraph(graphName);
	}

	@Override
	public void add(final Quad quad) {
		operate(quad, _add());
	}

	@Override
	public void delete(final Quad quad) {
		operate(quad, _delete());
	}

	/**
	 * Wraps a mutation to the state of this dataset with guards.
	 *
	 * @param quad the quad with which to mutate this dataset
	 * @param mutator the kind of change to make
	 */
	private void operate(final Quad quad, final Consumer<Quad> mutator) {
		if (isInTransaction())
			if (isTransactionType(WRITE))
				mutator.accept(quad);
			else throw new JenaTransactionException("Tried to write in a READ transaction!");
		else throw new JenaTransactionException("Tried to write outside of a transaction!");
	}

	/**
	 * @return a mutator that adds a quad to this dataset
	 */
	private final Consumer<Quad> _add() {
		return quad -> {
			if (!contains(quad)) {
				super.add(quad);
				if (!isAborting()) record.add(new QuadAddition(quad));
			}
		};
	}

	/**
	 * @return a mutator that deletes a quad from this dataset
	 */
	private final Consumer<Quad> _delete() {
		return quad -> {
			if (contains(quad)) {
				super.delete(quad);
				if (!isAborting()) record.add(new QuadDeletion(quad));
			}
		};
	}

	@Override
	public void add(final Node g, final Node s, final Node p, final Node o) {
		add(new Quad(g, s, p, o));
	}

	@Override
	public void delete(final Node g, final Node s, final Node p, final Node o) {
		delete(new Quad(g, s, p, o));
	}

	@Override
	public void deleteAny(final Node g, final Node s, final Node p, final Node o) {
		find(g, s, p, o).forEachRemaining(this::delete);
	}

	@Override
	public void clear() {
		deleteAny(ANY, ANY, ANY, ANY);
		super.clear();
	}

	@Override
	protected boolean abortImplemented() {
		return true;
	}

	@Override
	protected void _commit() {
		record.clear();
		super._commit();
	}

	@Override
	protected void _abort() {
		try {
			startAborting();
			if (isInTransaction() && isTransactionType(WRITE))
				record.reverse().consume(op -> op.inverse().actOn(this));
			else record.clear();
			super._abort();
		} finally {
			stopAborting();
		}
	}

	@Override
	protected void _end() {
		if (isInTransaction() && isTransactionType(WRITE)) abort();
		super._end();
	}

	@Override
	public void close() {
		record.clear();
		super.close();
	}
}

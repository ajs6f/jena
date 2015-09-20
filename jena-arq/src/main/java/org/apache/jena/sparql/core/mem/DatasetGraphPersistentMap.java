package org.apache.jena.sparql.core.mem;

import static java.lang.ThreadLocal.withInitial;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.sparql.core.GraphView.createDefaultGraph;
import static org.apache.jena.sparql.core.GraphView.createNamedGraph;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.MRPlusSWLock;
import org.apache.jena.sparql.core.DatasetGraphQuad;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;

public class DatasetGraphPersistentMap extends DatasetGraphQuad implements Transactional {

	private final Lock writeLock = new MRPlusSWLock();

	/**
	 * Commits must be atomic, and because a thread that is committing alters the various indexes one after another, we
	 * lock out {@link #begin(ReadWrite)} while {@link #commit()} is executing.
	 */
	private final ReentrantReadWriteLock commitLock = new ReentrantReadWriteLock(true);

	private final ThreadLocal<Boolean> isInTransaction = withInitial(() -> false);

	private final ThreadLocal<ReadWrite> transactionType = withInitial(() -> null);

	private final HexIndex index = new HexIndex();

	@Override
	public Lock getLock() {
		return writeLock;
	}

	@Override
	public void begin(final ReadWrite readWrite) {
		if (isInTransaction()) throw new JenaException("Transactions cannot be nested!");
		transactionType.set(readWrite);
		isInTransaction.set(true);
		getLock().enterCriticalSection(readWrite.equals(READ)); // get the dataset write lock, if needed.
		commitLock.readLock().lock(); // if a commit is proceeding, wait so that we see a coherent index state
		try {
			index.begin();
		} finally {
			commitLock.readLock().unlock();
		}
	}

	@Override
	public void commit() {
		commitLock.writeLock().lock();
		try {
			index.commit();
		} finally {
			commitLock.writeLock().unlock();
		}
		end();
	}

	@Override
	public void abort() {
		index.end();
		end();
	}

	@Override
	public void end() {
		isInTransaction.set(false);
		transactionType.set(null);
	}

	@Override
	public boolean isInTransaction() {
		return isInTransaction.get();
	}

	public ReadWrite transactionType() {
		return transactionType.get();
	}

	@Override
	public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o) {
		return index.find(g, s, p, o, true);
	}

	@Override
	public Iterator<Quad> findNG(final Node g, final Node s, final Node p, final Node o) {
		return index.find(g, s, p, o, false);
	}

	@Override
	public void add(final Quad q) {
		mutate(index::add, q);
	}

	@Override
	public void delete(final Quad q) {
		mutate(index::delete, q);
	}

	private void mutate(final Consumer<Quad> mutator, final Quad q) {
		if (!isInTransaction()) {
			// wrap this mutation in a WRITE transaction
			begin(WRITE);
			try {
				mutator.accept(q);
				commit();
			} finally {
				end();
			}
		} else if (transactionType().equals(WRITE)) mutator.accept(q);
		else throw new JenaException("Tried to write inside a READ transaction!");
	}

	@Override
	public Graph getDefaultGraph() {
		return createDefaultGraph(this);
	}

	@Override
	public Graph getGraph(final Node graphNode) {
		return createNamedGraph(this, graphNode);
	}

	@Override
	public Iterator<Node> listGraphNodes() {
		return index.listGraphNodes();
	}

	private Consumer<Graph> addGraph(final Node name) {
		return g -> g.find(ANY, ANY, ANY).forEachRemaining(t -> add(new Quad(name, t)));
	}

	private final Consumer<Graph> removeGraph = g -> g.find(ANY, ANY, ANY).forEachRemaining(g::delete);

	@Override
	public void addGraph(final Node graphName, final Graph graph) {
		mutateGraph(addGraph(graphName), graph);
	}

	@Override
	public void removeGraph(final Node graphName) {
		mutateGraph(removeGraph, getGraph(graphName));
	}

	private void mutateGraph(final Consumer<Graph> mutator, final Graph g) {
		if (!isInTransaction()) {
			// wrap this mutation in a WRITE transaction
			begin(WRITE);
			try {
				mutator.accept(g);
				commit();
			} finally {
				end();
			}
		} else if (transactionType().equals(WRITE)) mutator.accept(g);
		else throw new JenaException("Tried to write inside a READ transaction!");
	}
}

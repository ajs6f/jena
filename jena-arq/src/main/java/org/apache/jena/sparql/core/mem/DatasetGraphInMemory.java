/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.core.mem;

import static java.lang.ThreadLocal.withInitial;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.sparql.core.Quad.isUnionGraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.LockMRPlusSW;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphTriplesQuads;
import org.apache.jena.sparql.core.DatasetPrefixStorage;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;

/**
 * A {@link DatasetGraph} backed by an {@link QuadTable}. By default, this is a {@link HexIndex} designed for high-speed
 * in-memory operation.
 *
 */
public class DatasetGraphInMemory extends DatasetGraphTriplesQuads implements Transactional {

	private final DatasetPrefixStorage prefixes = new DatasetPrefixStorageInMemory();

	private final Lock writeLock = new LockMRPlusSW();

	/**
	 * Commits must be atomic, and because a thread that is committing alters the various indexes one after another, we
	 * lock out {@link #begin(ReadWrite)} while {@link #commit()} is executing.
	 */
	private final ReentrantReadWriteLock commitLock = new ReentrantReadWriteLock(true);

	private final ThreadLocal<Boolean> isInTransaction = withInitial(() -> false);

	private final ThreadLocal<ReadWrite> transactionType = withInitial(() -> null);

	private final QuadTable quadsIndex;

	private QuadTable quadsIndex() {
		return quadsIndex;
	}

	private final TripleTable dftGraph;

	private TripleTable dftGraphIndex() {
		return dftGraph;
	}

	@Override
	public Lock getLock() {
		return writeLock;
	}

	public DatasetGraphInMemory() {
		this(new HexIndex(), new TriIndex());
	}

	public DatasetGraphInMemory(final QuadTable i, final TripleTable t) {
		this.quadsIndex = i;
		this.dftGraph = t;
	}

	@Override
	public void begin(final ReadWrite readWrite) {
		if (isInTransaction()) throw new JenaTransactionException("Transactions cannot be nested!");
		transactionType.set(readWrite);
		isInTransaction.set(true);
		getLock().enterCriticalSection(readWrite.equals(READ)); // get the dataset write lock, if needed.
		commitLock.readLock().lock(); // if a commit is proceeding, wait so that we see a coherent index state
		try {
			quadsIndex().begin(readWrite);
		} finally {
			commitLock.readLock().unlock();
		}
	}

	@Override
	public void commit() {
		if (!isInTransaction()) throw new JenaTransactionException("Tried to commit outside a transaction!");
		commitLock.writeLock().lock();
		try {
			quadsIndex().commit();
			dftGraphIndex().commit();
		} finally {
			commitLock.writeLock().unlock();
		}
		end();
	}

	@Override
	public void abort() {
		if (!isInTransaction()) throw new JenaTransactionException("Tried to abort outside a transaction!");
		end();
	}

	@Override
	public void end() {
		quadsIndex().end();
		isInTransaction.set(false);
		transactionType.set(null);
		getLock().leaveCriticalSection();
	}

	@Override
	public boolean isInTransaction() {
		return isInTransaction.get();
	}

	public ReadWrite transactionType() {
		return transactionType.get();
	}

	private <T> Iterator<T> access(final Supplier<Iterator<T>> source) {
		if (!isInTransaction()) {
			begin(READ);
			try {
				return source.get();
			} finally {
				end();
			}
		}
		return source.get();
	}

	@Override
	public Iterator<Node> listGraphNodes() {
		return access(() -> quadsIndex().listGraphNodes());
	}

	private Iterator<Quad> quadsFinder(final Node g, final Node s, final Node p, final Node o) {
		if (isUnionGraph(g)) { // union graph is the merge of named graphs
			final Set<Triple> seen = new HashSet<>();
			return quadsIndex().find(ANY, s, p, o).filter(q -> !q.isDefaultGraph() && seen.add(q.asTriple())).iterator();
		}
		return quadsIndex().find(g, s, p, o).iterator();
	};

	private Iterator<Quad> triplesFinder(final Node s, final Node p, final Node o) {
		return triples2quadsDftGraph(dftGraphIndex().find(s, p, o).iterator());
	};

	@Override
	public void setDefaultGraph(final Graph g) {
		mutate(graph -> {
			dftGraphIndex().clear();
			graph.find(ANY, ANY, ANY)
					.forEachRemaining(t -> addToDftGraph(t.getSubject(), t.getPredicate(), t.getObject()));
		} , g);
	}

	@Override
	public Graph getGraph(final Node graphNode) {
		return new GraphInMemory(this, graphNode);
	}

	@Override
	public Graph getDefaultGraph() {
		return getGraph(Quad.defaultGraphNodeGenerated);
	}

	private Consumer<Graph> addGraph(final Node name) {
		return g -> g.find(ANY, ANY, ANY).forEachRemaining(t -> add(new Quad(name, t)));
	}

	private final Consumer<Graph> removeGraph = g -> g.find(ANY, ANY, ANY).forEachRemaining(g::delete);

	@Override
	public void addGraph(final Node graphName, final Graph graph) {
		mutate(addGraph(graphName), graph);
	}

	@Override
	public void removeGraph(final Node graphName) {
		mutate(removeGraph, getGraph(graphName));
	}

	/**
	 * Wrap a mutation in a WRITE transaction iff necessary.
	 *
	 * @param mutator
	 * @param payload
	 */
	private <T> void mutate(final Consumer<T> mutator, final T payload) {
		if (!isInTransaction()) {
			begin(WRITE);
			try {
				mutator.accept(payload);
				commit();
			} finally {
				end();
			}
		} else if (transactionType().equals(WRITE)) mutator.accept(payload);
		else throw new JenaTransactionException("Tried to write inside a READ transaction!");
	}

	public DatasetPrefixStorage getPrefixes() {
		return prefixes;
	}

	@Override
	protected void addToDftGraph(final Node s, final Node p, final Node o) {
		mutate(dftGraphIndex()::add, Triple.create(s, p, o));
	}

	@Override
	protected void addToNamedGraph(final Node g, final Node s, final Node p, final Node o) {
		mutate(quadsIndex()::add, Quad.create(g, s, p, o));
	}

	@Override
	protected void deleteFromDftGraph(final Node s, final Node p, final Node o) {
		mutate(dftGraphIndex()::delete, Triple.create(s, p, o));
	}

	@Override
	protected void deleteFromNamedGraph(final Node g, final Node s, final Node p, final Node o) {
		mutate(quadsIndex()::delete, Quad.create(g, s, p, o));
	}

	@Override
	protected Iterator<Quad> findInDftGraph(final Node s, final Node p, final Node o) {
		return access(() -> triplesFinder(s, p, o));
	}

	@Override
	protected Iterator<Quad> findInSpecificNamedGraph(final Node g, final Node s, final Node p, final Node o) {
		return access(() -> quadsFinder(g, s, p, o));
	}

	@Override
	protected Iterator<Quad> findInAnyNamedGraphs(final Node s, final Node p, final Node o) {
		return findInSpecificNamedGraph(ANY, s, p, o);
	}
}

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
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.sparql.core.GraphView.*;
import static org.apache.jena.sparql.core.Quad.defaultGraphNodeGenerated;
import static org.apache.jena.sparql.core.Quad.isUnionGraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.LockMRPlusSW;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphQuad;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;

/**
 * A {@link DatasetGraph} backed by an {@link Index}. By default, this is a {@link HexIndex} designed for high-speed
 * in-memory operation.
 *
 */
public class DatasetGraphInMemory extends DatasetGraphQuad implements Transactional {

	private final Lock writeLock = new LockMRPlusSW();

	/**
	 * Commits must be atomic, and because a thread that is committing alters the various indexes one after another, we
	 * lock out {@link #begin(ReadWrite)} while {@link #commit()} is executing.
	 */
	private final ReentrantReadWriteLock commitLock = new ReentrantReadWriteLock(true);

	private final ThreadLocal<Boolean> isInTransaction = withInitial(() -> false);

	private final ThreadLocal<ReadWrite> transactionType = withInitial(() -> null);

	private final Index index;

	private Index index() {
		return index;
	}

	@Override
	public Lock getLock() {
		return writeLock;
	}

	public DatasetGraphInMemory() {
		this(new HexIndex());
	}

	public DatasetGraphInMemory(final Index i) {
		this.index = i;
	}

	@Override
	public void begin(final ReadWrite readWrite) {
		if (isInTransaction()) throw new JenaException("Transactions cannot be nested!");
		transactionType.set(readWrite);
		isInTransaction.set(true);
		getLock().enterCriticalSection(readWrite.equals(READ)); // get the dataset write lock, if needed.
		commitLock.readLock().lock(); // if a commit is proceeding, wait so that we see a coherent index state
		try {
			index().begin(readWrite);
		} finally {
			commitLock.readLock().unlock();
		}
	}

	@Override
	public void commit() {
		commitLock.writeLock().lock();
		try {
			index().commit();
		} finally {
			commitLock.writeLock().unlock();
		}
		end();
	}

	@Override
	public void abort() {
		end();
	}

	@Override
	public void end() {
		index().end();
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

	@Override
	public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o) {
		return safeFind(g, s, p, o);
	}

	@Override
	public Iterator<Quad> findNG(final Node g, final Node s, final Node p, final Node o) {
		return safeFind(g, s, p, o);
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
		return access(() -> index().listGraphNodes());
	}

	private Iterator<Quad> finder(final Node g, final Node s, final Node p, final Node o) {
		if (isUnionGraph(g)) {
			// implement union graph functionality
			final Stream<Quad> quads = stream(spliteratorUnknownSize(index().find(ANY, s, p, o), 0), false);
			final Set<Triple> seenTriples = new HashSet<>();
			return quads.filter(q -> !q.isDefaultGraph() && seenTriples.add(q.asTriple())).iterator();
		}
		return index().find(g, s, p, o);
	};

	private Iterator<Quad> safeFind(final Node g, final Node s, final Node p, final Node o) {
		return access(() -> finder(g, s, p, o));
	}

	@Override
	public void add(final Quad q) {
		mutate(index()::add, q);
	}

	@Override
	public void delete(final Quad q) {
		mutate(index()::delete, q);
	}

	@Override
	public Graph getDefaultGraph() {
		return createDefaultGraph(this);
	}

	@Override
	public void setDefaultGraph(final Graph g) {
		mutate(graph -> {
			removeGraph(defaultGraphNodeGenerated);
			addGraph(defaultGraphNodeGenerated, graph);
		} , g);
	}

	@Override
	public Graph getGraph(final Node graphNode) {
		return isUnionGraph(graphNode) ? createUnionGraph(this) : createNamedGraph(this, graphNode);
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
		else throw new JenaException("Tried to write inside a READ transaction!");
	}
}

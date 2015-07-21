package org.apache.jena.sparql.core.journaling;

import static java.lang.ThreadLocal.withInitial;
import static org.apache.jena.ext.com.google.common.collect.Lists.newArrayList;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.query.ReadWrite.WRITE;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWithLock;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.journaling.OperationRecord.ReversibleOperationRecord;
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
	private ReversibleOperationRecord<QuadOperation> record = new ListBackedOperationRecord<>(new ArrayList<>());

	/**
	 * Indicates whether an transaction abort is in progress.
	 */
	private final ThreadLocal<Boolean> recording = withInitial(() -> false);

	private boolean isRecording() {
		return recording.get();
	}

	private void startRecording() {
		recording.set(true);
	}

	private void stopRecording() {
		recording.set(false);
	}

	/**
	 * @param dsg the DatasetGraph that will back this one
	 */
	public DatasetGraphWithRecord(final DatasetGraph dsg) {
		super(dsg);
	}

	/**
	 * @param dsg the DatasetGraph that will back this one
	 * @param record the operation record to use with this DatasetGraph
	 */
	public DatasetGraphWithRecord(final DatasetGraph dsg, final ReversibleOperationRecord<QuadOperation> record) {
		super(dsg);
		this.record = record;
	}

	@Override
	public void add(final Quad quad) {
		operate(quad, _add);
	}

	@Override
	public void delete(final Quad quad) {
		operate(quad, _delete);
	}

	@Override
	public void addGraph(final Node graphName, final Graph graph) {
		operateOnGraph(graphName, graph, _addGraph);
	}

	@Override
	public void removeGraph(final Node graphName) {
		operateOnGraph(graphName, null, _removeGraph);
	}

	/**
	 * A mutator that adds a graph to this dataset.
	 */
	private final BiConsumer<Node, Graph> _addGraph = (graphName, graph) -> {
		// create an empty graph in the backing store
		super.addGraph(graphName, new GraphMem());
		// copy all triples into it
		graph.find(ANY, ANY, ANY).forEachRemaining(t -> add(new Quad(graphName, t)));
	};

	/**
	 * A mutator that removes a graph from this dataset.
	 */
	private final BiConsumer<Node, Graph> _removeGraph = (graphName, graph) -> {
		// delete all triples in this graph in the backing store
		deleteAny(graphName, ANY, ANY, ANY);
		// remove the graph itself
		super.removeGraph(graphName);
	};

	/**
	 * A mutator that adds a quad to this dataset.
	 */
	private final Consumer<Quad> _add = quad -> {
		if (!contains(quad)) {
			super.add(quad);
			if (isRecording()) record.add(new QuadAddition(quad));
		}
	};

	/**
	 * A mutator that deletes a quad from this dataset.
	 */
	private final Consumer<Quad> _delete = quad -> {
		if (contains(quad)) {
			super.delete(quad);
			if (isRecording()) record.add(new QuadDeletion(quad));
		}
	};

	/**
	 * Guards a mutation to the state of this dataset.
	 *
	 * @param quad the quad with which to mutate this dataset
	 * @param mutator the kind of change to make
	 */
	private void operate(final Quad quad, final Consumer<Quad> mutator) {
		if (allowedToWrite())
			mutator.accept(quad);
		else throw new JenaTransactionException("Tried to write in a non-WRITE transaction!");
	}

	/**
	 * Guards a mutation to the state of this dataset.
	 *
	 * @param graphName the name of the graph on which to operate
	 * @param graph the graph on which to operate
	 * @param mutator the kind of change to make
	 */
	private void operateOnGraph(final Node graphName, final Graph graph, final BiConsumer<Node, Graph> mutator) {
		if (allowedToWrite())
			mutator.accept(graphName, graph);
		else throw new JenaTransactionException("Tried to write in a non_WRITE transaction!");
	}

	/**
	 * @return true iff we are outside a transaction or inside a WRITE transaction
	 */
	private boolean allowedToWrite() {
		return !isInTransaction() || isInTransaction() && isTransactionType(WRITE);
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
		newArrayList(find(g, s, p, o)).forEach(this::delete);
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
	protected void _begin(final ReadWrite readWrite) {
		super._begin(readWrite);
		startRecording();
	}

	@Override
	protected void _commit() {
		record.clear();
		super._commit();
	}

	@Override
	protected void _abort() {
		_end();
	}

	@Override
	protected void _end() {
		if (isInTransaction() && isTransactionType(WRITE)) {
			try {
				// pause recording operations from this thread
				stopRecording();
				// unwind the record
				record.reverse().consume(op -> op.inverse().actOn(this));
			} finally {
				// begin recording again
				startRecording();
			}
		}
		record.clear();
		super._end();
	}

	@Override
	public void close() {
		record.clear();
		super.close();
	}
}

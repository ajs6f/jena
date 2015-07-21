package org.apache.jena.sparql.core.journaling;

import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;
import org.junit.Assert;
import org.junit.Test;

public class TestDatasetGraphWithRecord extends Assert {
	private static final Node graphName = createURI("some-graph");
	private static final Node subject1 = createURI("subject1");
	private static final Node subject2 = createURI("subject2");
	private static final Node object1 = createURI("object1");
	private static final Node object2 = createURI("object2");
	private static final Node predicate = createURI("predicate");
	private static final Quad q1 = new Quad(graphName, subject1, predicate, object1);
	private static final Quad q2 = new Quad(graphName, subject1, predicate, object2);
	private static final Quad q3 = new Quad(graphName, subject2, predicate, object2);

	/**
	 * Adding a graph via {@link DatasetGraphWithRecord#addGraph(Node, org.apache.jena.graph.Graph)} should copy the
	 * tuples from that graph, instead of creating a reference to that graph.
	 */
	@Test
	public void testAddGraphCopiesTuples() {
		final GraphMem graph = new GraphMem();
		graph.add(q1.asTriple());
		graph.add(q2.asTriple());

		final DatasetGraph realDsg = new DatasetGraphWithRecord(new DatasetGraphMap(new GraphMem()));
		final Dataset dataset = DatasetFactory.create(realDsg);
		final DatasetGraph dsg = dataset.asDatasetGraph();

		dataset.begin(WRITE);
		try {
			dsg.addGraph(graphName, graph);
			dataset.commit();
		} finally {
			dataset.end();
		}

		assertTrue(dsg.contains(q1));
		assertTrue(dsg.contains(q2));
		// we add a new triple to our original graph
		graph.add(q3.asTriple());
		// which should not show up in the dataset, because of our "copy-on-addGraph" semantics
		assertFalse(dsg.contains(ANY, q3.getSubject(), q3.getPredicate(), q3.getObject()));
		dataset.close();
	}

	/**
	 * {@link Transactional#abort()} is properly supported.
	 */
	@Test
	public void testSimpleAbort() {
		final DatasetGraph realDsg = new DatasetGraphWithRecord(new DatasetGraphMap(new GraphMem()));
		final Dataset dataset = DatasetFactory.create(realDsg);
		final DatasetGraph dsg = dataset.asDatasetGraph();

		dataset.begin(WRITE);
		try {
			dsg.addGraph(graphName, new GraphMem());
			dsg.add(q1);
			dataset.commit();
		} finally {
			dataset.end();
		}
		try {
			dataset.begin(WRITE);
			dsg.add(q2);
			dataset.abort();
		} finally {
			dataset.end();
		}
		assertTrue(dsg.contains(q1));
		assertFalse(dsg.contains(q2));
		dataset.close();
	}

	/**
	 * Neither adding an already-present quad nor removing an absent quad should produce any change in the record.
	 */
	@Test
	public void testRecordShouldBeCompact() {
		final List<QuadOperation> record = new ArrayList<>();
		final DatasetGraph realDsg = new DatasetGraphWithRecord(new DatasetGraphMap(new GraphMem()),
				new ListBackedOperationRecord<>(record));
		final Dataset dataset = DatasetFactory.create(realDsg);
		final DatasetGraph dsg = dataset.asDatasetGraph();

		dataset.begin(WRITE);
		try {
			dsg.addGraph(graphName, new GraphMem());
			// add the same quad twice
			dsg.add(q1);
			dsg.add(q1);
			// now there should be only one operation in the journal
			assertEquals(1, record.size());

			dsg.delete(q1);
			// now there should be two operations in the journal
			assertEquals(2, record.size());
			// delete the quad we've already deleted
			dsg.delete(q1);
			// now there should still be only two operations in the journal
			assertEquals(2, record.size());
		} finally {
			dataset.end();
		}
		dataset.close();
	}

	/**
	 * {@link DatasetGraphWithRecord} can only be mutated within a transaction.
	 */
	@Test(expected = JenaTransactionException.class)
	public void testDatasetGraphWithRecordIsTransactionalOnlyForGraphWrites() {
		final DatasetGraph realDsg = new DatasetGraphWithRecord(new DatasetGraphMap(new GraphMem()));
		final Dataset dataset = DatasetFactory.create(realDsg);
		final DatasetGraph dsg = dataset.asDatasetGraph();

		dsg.addGraph(graphName, new GraphMem());
	}

	/**
	 * {@link DatasetGraphWithRecord} can only be mutated within a transaction.
	 */
	@Test(expected = JenaTransactionException.class)
	public void testDatasetGraphWithRecordIsTransactionalOnlyForTupleWrites() {
		final DatasetGraph realDsg = new DatasetGraphWithRecord(new DatasetGraphMap(new GraphMem()));
		final Dataset dataset = DatasetFactory.create(realDsg);
		final DatasetGraph dsg = dataset.asDatasetGraph();

		dsg.add(q1);
	}

	/**
	 * {@link DatasetGraphWithRecord} can only be mutated within a WRITE transaction.
	 */
	@Test(expected = JenaTransactionException.class)
	public void testDatasetGraphWithRecordIsWriteTransactionalOnlyForGraphWrites() {
		final DatasetGraph realDsg = new DatasetGraphWithRecord(new DatasetGraphMap(new GraphMem()));
		final Dataset dataset = DatasetFactory.create(realDsg);
		final DatasetGraph dsg = dataset.asDatasetGraph();

		dataset.begin(READ);
		try {
			dsg.addGraph(graphName, new GraphMem());
		} finally {
			dataset.end();
		}
	}

	/**
	 * {@link DatasetGraphWithRecord} can only be mutated within a WRITE transaction.
	 */
	@Test(expected = JenaTransactionException.class)
	public void testDatasetGraphWithRecordIsWriteTransactionalOnlyForTupleWrites() {
		final DatasetGraph realDsg = new DatasetGraphWithRecord(new DatasetGraphMap(new GraphMem()));
		final Dataset dataset = DatasetFactory.create(realDsg);
		final DatasetGraph dsg = dataset.asDatasetGraph();

		dataset.begin(READ);
		try {
			dsg.add(q1);
		} finally {
			dataset.end();
		}
	}

	@Test
	public void testRemoveGraph() {
		final DatasetGraph realDsg = new DatasetGraphWithRecord(new DatasetGraphMap(new GraphMem()));
		final Dataset dataset = DatasetFactory.create(realDsg);
		final DatasetGraph dsg = dataset.asDatasetGraph();

		dataset.begin(WRITE);
		try {
			dsg.addGraph(graphName, new GraphMem());
			dsg.add(q1);
			dataset.commit();
		} finally {
			dataset.end();
		}
		assertTrue(dsg.containsGraph(graphName));
		assertTrue(dsg.contains(q1));
		dataset.begin(WRITE);
		try {
			dsg.removeGraph(graphName);
			dataset.commit();
		} finally {
			dataset.end();
		}
		assertFalse(dsg.containsGraph(graphName));
		assertFalse(dsg.contains(ANY, q1.getSubject(), q1.getPredicate(), q1.getObject()));
		assertTrue(dsg.isEmpty());
	}

	@Test
	public void testClear() {
		final DatasetGraph realDsg = new DatasetGraphWithRecord(new DatasetGraphMap(new GraphMem()));
		final Dataset dataset = DatasetFactory.create(realDsg);
		final DatasetGraph dsg = dataset.asDatasetGraph();

		dataset.begin(WRITE);
		try {
			dsg.addGraph(graphName, new GraphMem());
			dsg.add(q1);
			dataset.commit();
		} finally {
			dataset.end();
		}
		assertTrue(dsg.contains(q1));
		dataset.begin(WRITE);
		try {
			dsg.clear();
			dataset.commit();
		} finally {
			dataset.end();
		}
		assertTrue(dsg.isEmpty());
	}
}

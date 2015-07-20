package org.apache.jena.sparql.core.journaling;

import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.graph.NodeFactory.createURI;

import org.apache.jena.graph.Node;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.Quad;
import org.junit.Assert;
import org.junit.Test;

public class TestDatasetGraphWithRecord extends Assert {
	private static final Node graphName = createURI("info:graph");
	private static final Node bNodeSubject1 = createBlankNode();
	private static final Node bNodeSubject2 = createBlankNode();
	private static final Node bNodeObject1 = createBlankNode();
	private static final Node bNodeObject2 = createBlankNode();
	private static final Node predicate = createURI("info:test");
	private static final Quad q1 = new Quad(graphName, bNodeSubject1, predicate, bNodeObject1);
	private static final Quad q2 = new Quad(graphName, bNodeSubject2, predicate, bNodeObject2);
	private static final Quad q3 = new Quad(graphName, bNodeSubject2, predicate, bNodeObject1);

	@Test
	public void testAbort() {
		final DatasetGraph realDsg = new DatasetGraphWithRecord(new DatasetGraphMap(new GraphMem()));
		final Dataset dataset = DatasetFactory.create(realDsg);

		dataset.begin(ReadWrite.WRITE);
		dataset.asDatasetGraph().addGraph(graphName, new GraphMem());
		dataset.asDatasetGraph().add(q1);
		dataset.commit();
		dataset.begin(ReadWrite.WRITE);
		dataset.asDatasetGraph().add(q2);
		dataset.abort();
		assertTrue(dataset.asDatasetGraph().contains(q1));
		assertFalse(dataset.asDatasetGraph().contains(q2));

	}
}

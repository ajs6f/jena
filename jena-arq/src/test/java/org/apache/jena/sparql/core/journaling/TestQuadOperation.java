package org.apache.jena.sparql.core.journaling;

import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.mockito.Mockito.verify;

import org.apache.jena.graph.Node;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.journaling.QuadOperation.QuadAddition;
import org.apache.jena.sparql.core.journaling.QuadOperation.QuadDeletion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestQuadOperation extends Assert {

	private static final Node graphName = createURI("info:graph");
	private static final Node bNodeSubject = createBlankNode();
	private static final Node bNodeObject = createBlankNode();
	private static final Node predicate = createURI("info:test");
	private static final Quad q = new Quad(graphName, bNodeSubject, predicate, bNodeObject);
	private static final QuadAddition quadAddition = new QuadAddition(q);
	private static final QuadDeletion quadDeletion = new QuadDeletion(q);

	@Mock
	private DatasetGraph mockDsg;

	@Test
	public void testActOn() {
		quadDeletion.actOn(mockDsg);
		verify(mockDsg).delete(q);
		quadAddition.actOn(mockDsg);
		verify(mockDsg).add(q);
	}

	@Test
	public void testInversion() {
		quadDeletion.inverse().actOn(mockDsg);
		verify(mockDsg).add(q);
		quadAddition.inverse().actOn(mockDsg);
		verify(mockDsg).delete(q);
	}

	@Test
	public void testInversionInSequence() {
		quadAddition.actOn(mockDsg);
		verify(mockDsg).add(q);
		quadAddition.inverse().actOn(mockDsg);
		verify(mockDsg).delete(q);
	}

	@Test
	public void testWithActualGraphStore() {
		final DatasetGraphMap realDsg = new DatasetGraphMap(new GraphMem());
		realDsg.addGraph(graphName, new GraphMem());
		quadAddition.actOn(realDsg);
		assertTrue(realDsg.contains(q));
		quadDeletion.actOn(realDsg);
		assertFalse(realDsg.contains(q));
		quadAddition.actOn(realDsg);
		assertTrue(realDsg.contains(q));
		quadAddition.inverse().actOn(realDsg);
		assertFalse(realDsg.contains(q));
	}
}

package org.apache.jena.sparql.core.mem;

import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toSet;
import static org.apache.jena.ext.com.google.common.collect.ImmutableSet.of;
import static org.apache.jena.ext.com.google.common.collect.Sets.powerSet;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.sparql.core.mem.IndexForm.*;
import static org.apache.jena.sparql.core.mem.QuadPattern.from;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.QuadPattern.Slot;
import org.junit.Test;

public class TestIndexForm {



	private static final Set<Set<Slot>> quadPatterns = powerSet(allOf(Slot.class));

	private static final Node concreteNode = createBlankNode();

	private static final QuadPattern allWildcardQuery = from(null, null, null, null);

	@Test
	public void testAllWildcardQueryCannotAvoidTraversal() {
		assertTrue(indexForms().noneMatch(form -> form.avoidsIteration(allWildcardQuery)));
	}

	@Test
	public void testAllNonWildcardQueriesCanAvoidTraversal() {
		assertTrue(quadPatterns.stream().map(QuadPattern::new).filter(qp -> !allWildcardQuery.equals(qp))
				.allMatch(this::avoidsIteration));
	}

	private boolean avoidsIteration(final QuadPattern qp) {
		return indexForms().anyMatch(form -> form.avoidsIteration(qp));
	}

	@Test
	public void testAvoidsIterationGSPO() {
		final Set<QuadPattern> correctAnswers = of(from(concreteNode, concreteNode, concreteNode, concreteNode),
				from(concreteNode, concreteNode, concreteNode, ANY), from(concreteNode, concreteNode, ANY, ANY),
				from(concreteNode, ANY, ANY, ANY));
		testAvoidsIteration(GSPO, correctAnswers);
	}

	@Test
	public void testAvoidsIterationGOPS() {
		final Set<QuadPattern> correctAnswers = of(from(concreteNode, concreteNode, concreteNode, concreteNode),
				from(concreteNode, ANY, ANY, concreteNode), from(concreteNode, ANY, concreteNode, concreteNode),
				from(concreteNode, ANY, ANY, ANY));
		testAvoidsIteration(GOPS, correctAnswers);
	}

	public void testAvoidsIteration(final IndexForm indexForm, final Set<QuadPattern> correctAnswers) {
		assertEquals(correctAnswers,
				quadPatterns.stream().map(QuadPattern::new).filter(indexForm::avoidsIteration).collect(toSet()));
	}

}

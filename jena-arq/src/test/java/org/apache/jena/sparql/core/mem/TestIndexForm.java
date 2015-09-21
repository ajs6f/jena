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
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.QuadPattern.Slot;
import org.junit.Test;

public class TestIndexForm {

	private static Stream<QuadPattern> quadPatterns() {
		return powerSet(allOf(Slot.class)).stream().map(QuadPattern::new);
	}

	private static final Node concreteNode = createBlankNode();

	private static final QuadPattern allWildcardQuery = from(null, null, null, null);

	@Test
	public void anAllWildcardQueryCannotAvoidTraversal() {
		assertTrue(indexForms().noneMatch(form -> form.avoidsTraversal(allWildcardQuery)));
	}

	@Test
	public void allQueriesWithAtLeastOneConcreteNodeCanAvoidTraversal() {
		assertTrue(quadPatterns().filter(qp -> !allWildcardQuery.equals(qp)).allMatch(this::canAvoidTraversal));
	}

	private boolean canAvoidTraversal(final QuadPattern qp) {
		return indexForms().anyMatch(form -> form.avoidsTraversal(qp));
	}

	@Test
	public void correctnessOfGSPO() {
		final Set<QuadPattern> correctAnswers = of(from(concreteNode, concreteNode, concreteNode, concreteNode),
				from(concreteNode, concreteNode, concreteNode, ANY), from(concreteNode, concreteNode, ANY, ANY),
				from(concreteNode, ANY, ANY, ANY));
		avoidsTraversal(GSPO, correctAnswers);
	}

	@Test
	public void correctnessOfGOPS() {
		final Set<QuadPattern> correctAnswers = of(from(concreteNode, concreteNode, concreteNode, concreteNode),
				from(concreteNode, ANY, ANY, concreteNode), from(concreteNode, ANY, concreteNode, concreteNode),
				from(concreteNode, ANY, ANY, ANY));
		avoidsTraversal(GOPS, correctAnswers);
	}

	public void avoidsTraversal(final IndexForm indexForm, final Set<QuadPattern> correctAnswers) {
		assertEquals(correctAnswers, quadPatterns().filter(indexForm::avoidsTraversal).collect(toSet()));
	}
}

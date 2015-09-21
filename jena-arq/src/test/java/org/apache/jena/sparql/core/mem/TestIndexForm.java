package org.apache.jena.sparql.core.mem;

import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toSet;
import static org.apache.jena.ext.com.google.common.collect.ImmutableSet.of;
import static org.apache.jena.ext.com.google.common.collect.Sets.powerSet;
import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.sparql.core.mem.IndexForm.*;
import static org.apache.jena.sparql.core.mem.IndexForm.Slot.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.IndexForm.Slot;
import org.junit.Test;

public class TestIndexForm {

	private static Stream<Set<IndexForm.Slot>> quadPatterns() {
		return powerSet(allOf(IndexForm.Slot.class)).stream();
	}

	private static final Node concreteNode = createBlankNode();

	private static final Set<IndexForm.Slot> allWildcardQuery = of();

	@Test
	public void anAllWildcardQueryCannotAvoidTraversal() {
		assertTrue(indexForms().noneMatch(form -> form.avoidsTraversal(allWildcardQuery)));
	}

	@Test
	public void allQueriesWithAtLeastOneConcreteNodeCanAvoidTraversal() {
		assertTrue(quadPatterns().filter(qp -> !allWildcardQuery.equals(qp)).allMatch(this::canAvoidTraversal));
	}

	private boolean canAvoidTraversal(final Set<IndexForm.Slot> qp) {
		return indexForms().anyMatch(form -> form.avoidsTraversal(qp));
	}

	@Test
	public void correctnessOfGSPO() {
		final Set<Set<Slot>> correctAnswers = of(of(GRAPH, SUBJECT, PREDICATE, OBJECT), of(GRAPH, SUBJECT, PREDICATE),
				of(GRAPH, SUBJECT), of(GRAPH));
		avoidsTraversal(GSPO, correctAnswers);
	}

	@Test
	public void correctnessOfGOPS() {
		final Set<Set<Slot>> correctAnswers = of(of(GRAPH, SUBJECT, PREDICATE, OBJECT), of(GRAPH, OBJECT),
				of(GRAPH, PREDICATE, OBJECT), of(GRAPH));
		avoidsTraversal(GOPS, correctAnswers);
	}

	public void avoidsTraversal(final IndexForm indexForm, final Set<Set<Slot>> correctAnswers) {
		assertEquals(correctAnswers, quadPatterns().filter(indexForm::avoidsTraversal).collect(toSet()));
	}

}

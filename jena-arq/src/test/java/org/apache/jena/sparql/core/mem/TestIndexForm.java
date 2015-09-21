package org.apache.jena.sparql.core.mem;

import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toSet;
import static org.apache.jena.ext.com.google.common.collect.ImmutableSet.of;
import static org.apache.jena.ext.com.google.common.collect.Sets.newHashSet;
import static org.apache.jena.ext.com.google.common.collect.Sets.powerSet;
import static org.apache.jena.sparql.core.mem.IndexForm.*;
import static org.apache.jena.sparql.core.mem.IndexForm.Slot.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.sparql.core.mem.IndexForm.Slot;
import org.junit.Test;

public class TestIndexForm {

	private static Stream<Set<Slot>> queryPatterns() {
		return powerSet(allOf(Slot.class)).stream();
	}

	private static final Set<Slot> allWildcardQuery = of();

	@Test
	public void anAllWildcardQueryCannotAvoidTraversal() {
		assertTrue(indexForms().noneMatch(form -> form.avoidsTraversal(allWildcardQuery)));
	}

	@Test
	public void allQueriesWithAtLeastOneConcreteNodeCanAvoidTraversal() {
		assertTrue(queryPatterns().filter(p -> !allWildcardQuery.equals(p)).allMatch(this::canAvoidTraversal));
	}

	private boolean canAvoidTraversal(final Set<Slot> pattern) {
		return indexForms().anyMatch(form -> form.avoidsTraversal(pattern));
	}

	@Test
	public void anyIndexCanAnswerAnEntirelyConcretePattern() {
		indexForms().allMatch(form -> form.avoidsTraversal(allWildcardQuery));
	}

	@Test
	public void correctnessOfGSPO() {
		final Set<Set<Slot>> correctAnswers = key(of(GRAPH, SUBJECT, PREDICATE), of(GRAPH, SUBJECT), of(GRAPH));
		avoidsTraversal(GSPO, correctAnswers);
	}

	@Test
	public void correctnessOfGOPS() {
		final Set<Set<Slot>> correctAnswers = key(of(GRAPH, OBJECT), of(GRAPH, PREDICATE, OBJECT), of(GRAPH));
		avoidsTraversal(GOPS, correctAnswers);
	}

	@Test
	public void correctnessOfOPSG() {
		final Set<Set<Slot>> correctAnswers = key(of(PREDICATE, OBJECT), of(SUBJECT, PREDICATE, OBJECT), of(OBJECT));
		avoidsTraversal(OPSG, correctAnswers);
	}

	@Test
	public void correctnessOfOSGP() {
		final Set<Set<Slot>> correctAnswers = key(of(SUBJECT, OBJECT), of(SUBJECT, GRAPH, OBJECT), of(OBJECT));
		avoidsTraversal(OSGP, correctAnswers);
	}

	@Test
	public void correctnessOfPGSO() {
		final Set<Set<Slot>> correctAnswers = key(of(PREDICATE, GRAPH), of(PREDICATE, GRAPH, SUBJECT), of(PREDICATE));
		avoidsTraversal(PGSO, correctAnswers);
	}

	@Test
	public void correctnessOfSPOG() {
		final Set<Set<Slot>> correctAnswers = key(of(PREDICATE, SUBJECT), of(PREDICATE, OBJECT, SUBJECT), of(SUBJECT));
		avoidsTraversal(SPOG, correctAnswers);
	}

	@SafeVarargs
	private final Set<Set<Slot>> key(final Set<Slot>... answers) {
		final Set<Set<Slot>> key = newHashSet(answers);
		key.add(of(GRAPH, SUBJECT, PREDICATE, OBJECT)); // every index can answer a fully-concrete query pattern
		return key;
	}

	private void avoidsTraversal(final IndexForm indexForm, final Set<Set<Slot>> correctAnswers) {
		final Set<Set<Slot>> answers = queryPatterns().filter(indexForm::avoidsTraversal).collect(toSet());
		assertEquals(correctAnswers, answers);
	}

	private static Map<Set<Slot>, Set<IndexForm>> answerKey = new HashMap<Set<Slot>, Set<IndexForm>>() {
		{
			put(of(GRAPH), of(GSPO, GOPS));
			put(of(GRAPH, SUBJECT), of(GSPO));
			put(of(GRAPH, SUBJECT, PREDICATE), of(GSPO));
			put(of(GRAPH, SUBJECT, OBJECT), of(OSGP));
			put(of(SUBJECT), of(SPOG));
			put(of(PREDICATE), of(PGSO));
			put(of(GRAPH, PREDICATE), of(PGSO));
			put(of(SUBJECT, PREDICATE), of(SPOG));
			put(of(OBJECT), of(OPSG, OSGP));
			put(of(GRAPH, OBJECT), of(GOPS));
			put(of(SUBJECT, OBJECT), of(OSGP));
			put(of(PREDICATE, OBJECT), of(OPSG));
			put(of(GRAPH, PREDICATE, OBJECT), of(GOPS));
			put(of(SUBJECT, PREDICATE, OBJECT), of(SPOG));
			put(of(SUBJECT, PREDICATE, OBJECT, GRAPH), of(GSPO, GOPS, SPOG, OPSG, OSGP, PGSO));
			put(of(), of(GSPO));

		}
	};

	@Test
	public void aCorrectIndexIsChosenForEachPattern() {
		answerKey.forEach((sample, correctAnswers) -> {
			assertTrue(correctAnswers.contains(IndexForm.chooseFrom(sample)));
		});
	}
}

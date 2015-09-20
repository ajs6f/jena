package org.apache.jena.sparql.core.mem;

import static java.util.EnumSet.copyOf;
import static java.util.EnumSet.noneOf;
import static java.util.Objects.hash;
import static org.apache.jena.sparql.core.mem.QuadPattern.Slot.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.QuadPattern.Slot;

public class QuadPattern implements Predicate<Collection<Slot>> {

	private final Set<Slot> slots;

	public static enum Slot {
		GRAPH, SUBJECT, PREDICATE, OBJECT;
	}

	public QuadPattern(final Set<Slot> s) {
		this.slots = s;
	}

	public static QuadPattern from(final Node g, final Node s, final Node p, final Node o) {
		final EnumSet<Slot> pattern = noneOf(Slot.class);
		if (isConcrete(g)) pattern.add(GRAPH);
		if (isConcrete(s)) pattern.add(SUBJECT);
		if (isConcrete(p)) pattern.add(PREDICATE);
		if (isConcrete(o)) pattern.add(OBJECT);
		return new QuadPattern(pattern);
	}

	private static boolean isConcrete(final Node n) {
		return n != null && n.isConcrete();
	}

	@Override
	public boolean test(final Collection<Slot> m) {
		return slots.equals(copyOf(m));
	}

	@Override
	public String toString() {
		return slots.toString();
	}

	@Override
	public int hashCode() {
		return hash(slots);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return Objects.equals(slots, ((QuadPattern) obj).slots);
	}
}

package org.apache.jena.sparql.core.mem;

import static java.util.EnumSet.noneOf;
import static java.util.EnumSet.of;
import static java.util.Objects.hash;
import static org.apache.jena.ext.com.google.common.collect.Sets.immutableEnumSet;
import static org.apache.jena.sparql.core.mem.QuadPattern.Slot.GRAPH;
import static org.apache.jena.sparql.core.mem.QuadPattern.Slot.OBJECT;
import static org.apache.jena.sparql.core.mem.QuadPattern.Slot.PREDICATE;
import static org.apache.jena.sparql.core.mem.QuadPattern.Slot.SUBJECT;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.QuadPattern.Slot;

public class QuadPattern implements Set<Slot> {

	private Set<Slot> slots = noneOf(Slot.class);

	public static enum Slot {
		GRAPH, SUBJECT, PREDICATE, OBJECT;
	}

	private QuadPattern(EnumSet<Slot> s) {
		this.slots = immutableEnumSet(s);
	}

	private QuadPattern(Slot s1, Slot... s) {
		this.slots = immutableEnumSet(of(s1, s));
	}

	public static QuadPattern from(Slot s1, Slot... s) {
		return new QuadPattern(s1, s);
	}

	public static QuadPattern from(Node g, Node s, Node p, Node o) {
		final EnumSet<Slot> pattern = noneOf(Slot.class);
		if (g != null) pattern.add(GRAPH);
		if (s != null) pattern.add(SUBJECT);
		if (p != null) pattern.add(PREDICATE);
		if (o != null) pattern.add(OBJECT);
		return new QuadPattern(pattern);
	}

	@Override
	public int size() {
		return slots.size();
	}

	@Override
	public boolean isEmpty() {
		return slots.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return slots.contains(o);
	}

	@Override
	public Iterator<Slot> iterator() {
		return slots.iterator();
	}

	@Override
	public Object[] toArray() {
		return slots.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return slots.toArray(a);
	}

	@Override
	public boolean add(Slot e) {
		return slots.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return slots.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return slots.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Slot> c) {
		return slots.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return slots.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return slots.removeAll(c);
	}

	@Override
	public void clear() {
		slots.clear();
	}

	@Override
	public int hashCode() {
		return hash(slots);
	}

	public boolean matches(EnumSet<Slot> m) {
		return slots.equals(m);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || !(obj instanceof QuadPattern)) return false;
		return slots.equals(((QuadPattern) obj).slots);
	}
}

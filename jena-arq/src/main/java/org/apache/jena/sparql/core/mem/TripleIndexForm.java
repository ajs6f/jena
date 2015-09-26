package org.apache.jena.sparql.core.mem;

import static java.util.Arrays.stream;
import static java.util.EnumSet.of;
import static org.apache.jena.sparql.core.mem.Slot.*;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public enum TripleIndexForm implements Supplier<TripleTable> {

	SPO(of(SUBJECT, PREDICATE), SUBJECT) {
		@Override
		public TripleTable get() {
			return new PMapTripleTable(name()) {

				@Override
				public Stream<Triple> find(final Node s, final Node p, final Node o) {
					return _find(s, p, o);
				}

				@Override
				public void add(final Triple t) {
					_add(t.getSubject(), t.getPredicate(), t.getObject());
				}

				@Override
				public void delete(final Triple t) {
					_delete(t.getSubject(), t.getPredicate(), t.getObject());
				}

			};
		}

	},
	POS(of(PREDICATE, OBJECT), PREDICATE) {

		@Override
		public TripleTable get() {
			return new PMapTripleTable(name()) {

				@Override
				public Stream<Triple> find(final Node s, final Node p, final Node o) {
					return _find(p, o, s);
				}

				@Override
				public void add(final Triple t) {
					_add(t.getPredicate(), t.getObject(), t.getSubject());
				}

				@Override
				public void delete(final Triple t) {
					_delete(t.getPredicate(), t.getObject(), t.getSubject());
				}

			};
		}
	},
	OSP(of(OBJECT, SUBJECT), OBJECT) {

		@Override
		public TripleTable get() {
			return new PMapTripleTable(name()) {

				@Override
				public Stream<Triple> find(final Node s, final Node p, final Node o) {
					return _find(o, s, p);
				}

				@Override
				public void add(final Triple t) {
					_add(t.getObject(), t.getSubject(), t.getPredicate());
				}

				@Override
				public void delete(final Triple t) {
					_delete(t.getObject(), t.getSubject(), t.getPredicate());
				}

			};
		}
	};
	private TripleIndexForm(final Set<Slot> tp, final Slot op) {
		this.twoPattern = tp;
		this.onePattern = of(op);
	}

	public final Set<Slot> twoPattern, onePattern;

	public boolean avoidsTraversal(final Set<Slot> pattern) {
		return twoPattern.equals(pattern) || onePattern.equals(pattern);
	}

	public static TripleIndexForm chooseFrom(final Set<Slot> pattern) {
		return indexForms().filter(f -> f.avoidsTraversal(pattern)).findFirst().orElse(SPO);
	}

	public static Stream<TripleIndexForm> indexForms() {
		return stream(values());
	}
}

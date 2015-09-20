package org.apache.jena.sparql.core.mem;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.generate;
import static org.apache.jena.sparql.core.mem.QuadPattern.Slot.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.QuadPattern.Slot;

/**
 * The six covering index forms and machinery to determine which of them is best suited to answer a given query.
 *
 */
public enum IndexForm implements Supplier<Index> {

	GSPO {
		@Override
		public Index get() {
			return new Index() {
				@Override
				public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o,
						final boolean unused) {
					return _find(g, s, p, o);
				}

				@Override
				public void add(final Quad q) {
					add(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
				}

				@Override
				public void delete(final Quad q) {
					delete(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
				}
			};
		}

		@Override
		public boolean avoidsIteration(final QuadPattern qp) {
			return avoidsIteration(qp, asList(GRAPH, SUBJECT, PREDICATE, OBJECT));
		}
	},
	GOPS {
		@Override
		public Index get() {
			return new Index() {

				@Override
				public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o,
						final boolean unused) {
					return _find(g, o, p, s);
				}

				@Override
				public void add(final Quad q) {
					add(q.getGraph(), q.getObject(), q.getPredicate(), q.getSubject());
				}

				@Override
				public void delete(final Quad q) {
					delete(q.getGraph(), q.getObject(), q.getPredicate(), q.getSubject());
				}
			};

		}

		@Override
		public boolean avoidsIteration(final QuadPattern qp) {
			return avoidsIteration(qp, asList(GRAPH, OBJECT, PREDICATE, SUBJECT));
		}
	},
	SPOG {
		@Override
		public Index get() {
			return new Index() {

				@Override
				public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o,
						final boolean unused) {
					return _find(s, p, o, g);
				}

				@Override
				public void add(final Quad q) {
					add(q.getSubject(), q.getPredicate(), q.getObject(), q.getGraph());
				}

				@Override
				public void delete(final Quad q) {
					delete(q.getSubject(), q.getPredicate(), q.getObject(), q.getGraph());
				}
			};
		}

		@Override
		public boolean avoidsIteration(final QuadPattern qp) {
			return avoidsIteration(qp, asList(SUBJECT, PREDICATE, OBJECT, GRAPH));
		}
	},
	OSGP {
		@Override
		public Index get() {
			return new Index() {

				@Override
				public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o,
						final boolean unused) {
					return _find(o, s, g, p);
				}

				@Override
				public void add(final Quad q) {
					add(q.getObject(), q.getSubject(), q.getGraph(), q.getPredicate());
				}

				@Override
				public void delete(final Quad q) {
					delete(q.getObject(), q.getSubject(), q.getGraph(), q.getPredicate());
				}
			};
		}

		@Override
		public boolean avoidsIteration(final QuadPattern qp) {
			return avoidsIteration(qp, asList(OBJECT, SUBJECT, GRAPH, PREDICATE));
		}
	},
	PGSO {
		@Override
		public Index get() {
			return new Index() {

				@Override
				public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o,
						final boolean searchDefaultGraph) {
					return _find(p, g, s, o);
				}

				@Override
				public void add(final Quad q) {
					add(q.getPredicate(), q.getGraph(), q.getSubject(), q.getObject());
				}

				@Override
				public void delete(final Quad q) {
					delete(q.getPredicate(), q.getGraph(), q.getSubject(), q.getObject());
				}
			};
		}

		@Override
		public boolean avoidsIteration(final QuadPattern qp) {
			return avoidsIteration(qp, asList(PREDICATE, GRAPH, SUBJECT, OBJECT));
		}
	},
	OPSG {
		@Override
		public Index get() {
			return new Index() {

				@Override
				public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o,
						final boolean searchDefaultGraph) {
					return _find(o, p, s, g);
				}

				@Override
				public void add(final Quad q) {
					add(q.getObject(), q.getPredicate(), q.getSubject(), q.getGraph());
				}

				@Override
				public void delete(final Quad q) {
					delete(q.getObject(), q.getPredicate(), q.getSubject(), q.getGraph());
				}
			};
		}

		@Override
		public boolean avoidsIteration(final QuadPattern qp) {
			return avoidsIteration(qp, asList(OBJECT, PREDICATE, SUBJECT, GRAPH));
		}
	};

	public abstract boolean avoidsIteration(final QuadPattern qp);

	private static boolean avoidsIteration(final QuadPattern qp, final List<Slot> fullPattern) {
		final AtomicInteger i = new AtomicInteger(4);
		return generate(() -> i.getAndDecrement()).limit(4).map(index -> fullPattern.subList(0, index)).anyMatch(qp);
	}

	public static IndexForm choose(final QuadPattern qp) {
		return indexForms().filter(f -> f.avoidsIteration(qp)).findFirst()
				.orElseThrow(() -> new JenaException("No index available for impossible query pattern!"));
	}

	public static Stream<IndexForm> indexForms() {
		return stream(values());
	}

}

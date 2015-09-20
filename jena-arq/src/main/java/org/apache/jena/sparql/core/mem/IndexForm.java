package org.apache.jena.sparql.core.mem;

import static java.util.Arrays.stream;
import static java.util.EnumSet.of;
import static org.apache.jena.ext.com.google.common.collect.ImmutableSet.of;
import static org.apache.jena.ext.com.google.common.collect.Maps.immutableEnumMap;
import static org.apache.jena.sparql.core.mem.QuadPattern.Slot.GRAPH;
import static org.apache.jena.sparql.core.mem.QuadPattern.Slot.OBJECT;
import static org.apache.jena.sparql.core.mem.QuadPattern.Slot.PREDICATE;
import static org.apache.jena.sparql.core.mem.QuadPattern.Slot.SUBJECT;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
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
			return new GraphSubjectPredicateObjectIndex();
		}
	},
	GOPS {
		@Override
		public Index get() {
			return new Index() {

				@Override
				public Iterator<Quad> find(Node g, Node s, Node p, Node o, boolean searchDefaultGraph) {
					return _find(g, o, p, s);
				}

				@Override
				public void add(Quad q) {
					add(q.getGraph(), q.getObject(), q.getPredicate(), q.getSubject());
				}

				@Override
				public void delete(Quad q) {
					delete(q.getGraph(), q.getObject(), q.getPredicate(), q.getSubject());
				}
			};
		}
	},
	SPOG {
		@Override
		public Index get() {
			return new GraphSubjectPredicateObjectIndex();
		}
	},
	OSGP {
		@Override
		public Index get() {
			return new GraphSubjectPredicateObjectIndex();
		}
	},
	PGSO {
		@Override
		public Index get() {
			return new GraphSubjectPredicateObjectIndex();
		}
	},
	OPSG {
		@Override
		public Index get() {
			return new GraphSubjectPredicateObjectIndex();
		}
	};

	public boolean avoidsIteration(final QuadPattern qp) {
		return selector.get(this).stream().anyMatch(qp::matches);
	}

	public static IndexForm choose(QuadPattern qp) {
		return forms().filter(f -> f.avoidsIteration(qp)).findFirst().orElseThrow(() -> new JenaException());
	}

	public static Stream<IndexForm> forms() {
		return stream(values());
	}

	private static Map<IndexForm, Set<EnumSet<Slot>>> selector = immutableEnumMap(
			ImmutableMap.<IndexForm, Set<EnumSet<Slot>>> builder()
					.put(GSPO,
							of(of(GRAPH), of(GRAPH, SUBJECT), of(GRAPH, SUBJECT, PREDICATE),
									of(GRAPH, SUBJECT, PREDICATE, OBJECT)))
					.put(GOPS,
							of(of(GRAPH), of(GRAPH, OBJECT), of(GRAPH, OBJECT, PREDICATE),
									of(GRAPH, OBJECT, PREDICATE, SUBJECT)))
			.put(SPOG, of(of(SUBJECT), of(SUBJECT, PREDICATE), of(SUBJECT, PREDICATE, OBJECT),
					of(SUBJECT, PREDICATE, OBJECT, GRAPH))).build());

}

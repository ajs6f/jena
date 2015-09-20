package org.apache.jena.sparql.core.mem;

import static java.lang.ThreadLocal.withInitial;
import static java.util.Collections.emptyIterator;
import static org.apache.jena.atlas.iterator.Iter.singleton;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.atlas.lib.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;
import org.apache.jena.sparql.core.mem.FourTupleMap.TwoTupleMap;

public abstract class Index {

	private final AtomicReference<FourTupleMap> index = new AtomicReference<>(FourTupleMap.empty());

	private AtomicReference<FourTupleMap> master() {
		return index;
	}

	ThreadLocal<FourTupleMap> local;

	public void begin() {
		local = withInitial(() -> master().get());
	}

	public void end() {
		local = null;
	}

	public abstract Iterator<Quad> find(Node g, Node s, Node p, Node o, boolean searchDefaultGraph);

	protected Iterator<Quad> _find(final Node first, final Node second, final Node third, final Node fourth) {
		final FourTupleMap indexMap = local.get();
		if (first != null) {
			// a specific graph
			if (!indexMap.containsKey(first)) return emptyIterator();
			final ThreeTupleMap graph = indexMap.get(first);
			if (second != null) {
				// a specific subject
				if (!graph.containsKey(second)) return emptyIterator();
				final TwoTupleMap predicates = graph.get(second);
				if (third != null) {
					// a specific predicate
					if (!predicates.containsKey(third)) return emptyIterator();
					final PersistentSet<Node> objects = predicates.get(third);
					if (fourth != null) {
						// a specific object
						if (!objects.contains(fourth)) return emptyIterator();
						return singleton(Quad.create(first, second, third, fourth));
					}
					// wildcard object
					return objects.stream().map(ob -> Quad.create(first, second, third, ob)).iterator();
				}
				// wildcard predicate
				return predicates.entrySet().stream()
						.flatMap(e -> e.getValue().stream().map(ob -> Quad.create(first, second, e.getKey(), ob)))
						.iterator();
			}
			// wildcard subject
			return graph.entrySet().stream()
					.flatMap(e -> e.getValue().entrySet().stream().flatMap(
							ep -> ep.getValue().stream().map(ob -> Quad.create(first, e.getKey(), ep.getKey(), ob))))
					.iterator();
		}
		return indexMap.entrySet().stream()
				.flatMap(
						eg -> eg.getValue().entrySet().stream()
								.flatMap(e -> e.getValue().entrySet().stream()
										.flatMap(ep -> ep.getValue().stream()
												.map(ob -> Quad.create(eg.getKey(), e.getKey(), ep.getKey(), ob)))))
				.iterator();

	}

	public abstract void add(Quad q);

	public void add(final Node first, final Node second, final Node third, final Node fourth) {
		FourTupleMap indexMap = local.get();
		if (!indexMap.containsKey(first)) indexMap = indexMap.plus(first, ThreeTupleMap.empty());

		ThreeTupleMap threeTuples = indexMap.get(first);
		if (!threeTuples.containsKey(second)) threeTuples = threeTuples.plus(second, TwoTupleMap.empty());

		TwoTupleMap twoTuples = threeTuples.get(third);
		if (!twoTuples.containsKey(third)) twoTuples = twoTuples.plus(third, PersistentSet.empty());

		PersistentSet<Node> oneTuples = twoTuples.get(third);
		if (!oneTuples.contains(fourth)) oneTuples = oneTuples.plus(fourth);

		twoTuples = twoTuples.minus(third).plus(third, oneTuples);
		threeTuples = threeTuples.minus(second).plus(second, twoTuples);
		local.set(indexMap.minus(first).plus(first, threeTuples));
	}

	public abstract void delete(Quad q);

	public void delete(final Node first, final Node second, final Node third, final Node fourth) {
		final FourTupleMap indexMap = local.get();
		if (indexMap.containsKey(first)) {
			ThreeTupleMap threeTuples = indexMap.get(first);
			if (threeTuples.containsKey(second)) {
				TwoTupleMap twoTuples = threeTuples.get(third);
				if (twoTuples.containsKey(third)) {
					PersistentSet<Node> oneTuples = twoTuples.get(third);
					if (oneTuples.contains(fourth)) {
						oneTuples = oneTuples.minus(fourth);
						twoTuples = twoTuples.minus(third).plus(third, oneTuples);
						threeTuples = threeTuples.minus(second).plus(second, twoTuples);
						local.set(indexMap.minus(first).plus(first, threeTuples));
					}
				}
			}
		}
	}

	synchronized public void commit() {
		master().set(local.get());
		end();
	}
}

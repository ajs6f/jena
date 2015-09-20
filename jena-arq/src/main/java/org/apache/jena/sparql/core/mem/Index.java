package org.apache.jena.sparql.core.mem;

import static java.lang.ThreadLocal.withInitial;
import static java.util.Collections.emptyIterator;
import static org.apache.jena.atlas.iterator.Iter.singleton;
import static org.apache.jena.sparql.core.Quad.create;

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
		final FourTupleMap fourTuples = local.get();
		if (first != null) {
			// a specific first slot value
			if (!fourTuples.containsKey(first)) return emptyIterator();
			final ThreeTupleMap threeTuples = fourTuples.get(first);
			if (second != null) {
				// a specific second slot value
				if (!threeTuples.containsKey(second)) return emptyIterator();
				final TwoTupleMap twoTuples = threeTuples.get(second);
				if (third != null) {
					// a specific third slot value
					if (!twoTuples.containsKey(third)) return emptyIterator();
					final PersistentSet<Node> oneTuples = twoTuples.get(third);
					if (fourth != null) {
						// a specific fourth slot value
						if (!oneTuples.contains(fourth)) return emptyIterator();
						return singleton(create(first, second, third, fourth));
					}
					// wildcard fourth slot
					return oneTuples.stream().map(slot4 -> create(first, second, third, slot4)).iterator();
				}
				// wildcard third slot
				return twoTuples
						.descend(e -> e.getValue().stream().map(slot4 -> create(first, second, e.getKey(), slot4)))
						.iterator();
			}
			// wildcard second slot
			return threeTuples
					.descend(
							slot2 -> slot2.getValue()
									.descend(slot3 -> slot3.getValue().stream()
											.map(slot4 -> create(first, slot2.getKey(), slot3.getKey(), slot4))))
					.iterator();
		}
		// wildcard everything
		return fourTuples
				.descend(slot1 -> slot1.getValue()
						.descend(slot2 -> slot2.getValue()
								.descend(slot3 -> slot3.getValue().stream()
										.map(slot4 -> create(slot1.getKey(), slot2.getKey(), slot3.getKey(), slot4)))))
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
		final FourTupleMap fourTuples = local.get();
		if (fourTuples.containsKey(first)) {
			ThreeTupleMap threeTuples = fourTuples.get(first);
			if (threeTuples.containsKey(second)) {
				TwoTupleMap twoTuples = threeTuples.get(third);
				if (twoTuples.containsKey(third)) {
					PersistentSet<Node> oneTuples = twoTuples.get(third);
					if (oneTuples.contains(fourth)) {
						oneTuples = oneTuples.minus(fourth);
						twoTuples = twoTuples.minus(third).plus(third, oneTuples);
						threeTuples = threeTuples.minus(second).plus(second, twoTuples);
						local.set(fourTuples.minus(first).plus(first, threeTuples));
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

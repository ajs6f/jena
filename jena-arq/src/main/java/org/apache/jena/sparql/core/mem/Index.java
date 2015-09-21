package org.apache.jena.sparql.core.mem;

import static java.lang.ThreadLocal.withInitial;
import static java.util.Collections.emptyIterator;
import static org.apache.jena.atlas.iterator.Iter.singleton;
import static org.apache.jena.sparql.core.Quad.create;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.atlas.lib.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;
import org.apache.jena.sparql.core.mem.FourTupleMap.TwoTupleMap;
import org.slf4j.Logger;

public abstract class Index {

	private static final Logger log = getLogger(Index.class);

	private final AtomicReference<FourTupleMap> index = new AtomicReference<>(FourTupleMap.empty());

	private AtomicReference<FourTupleMap> master() {
		return index;
	}

	ThreadLocal<FourTupleMap> local;

	public void begin() {
		// capture transactional reference
		local = withInitial(() -> master().get());
	}

	public void end() {
		// abandon transactional reference
		local = null;
	}

	public abstract Iterator<Quad> find(Node g, Node s, Node p, Node o, boolean searchDefaultGraph);

	protected Iterator<Quad> _find(final Node first, final Node second, final Node third, final Node fourth) {
		log.debug("Querying on four-tuple pattern: {} {} {} {} .", first, second, third, fourth);
		final FourTupleMap fourTuples = local.get();
		if (first != null && first.isConcrete()) {
			// a specific first slot value
			if (!fourTuples.containsKey(first)) return emptyIterator();
			final ThreeTupleMap threeTuples = fourTuples.get(first);
			if (second != null && second.isConcrete()) {
				// a specific second slot value
				if (!threeTuples.containsKey(second)) return emptyIterator();
				final TwoTupleMap twoTuples = threeTuples.get(second);
				if (third != null && third.isConcrete()) {
					// a specific third slot value
					if (!twoTuples.containsKey(third)) return emptyIterator();
					final PersistentSet<Node> oneTuples = twoTuples.get(third);
					if (fourth != null && fourth.isConcrete()) {
						// a specific fourth slot value
						if (!oneTuples.contains(fourth)) return emptyIterator();
						return singleton(create(first, second, third, fourth));
					}
					// wildcard fourth slot
					return oneTuples.stream().map(slot4 -> create(first, second, third, slot4)).iterator();
				}
				// wildcard third slot
				return twoTuples.descend(
						(slot3, oneTuples) -> oneTuples.stream().map(slot4 -> create(first, second, slot3, slot4)))
						.iterator();
			}
			// wildcard second slot
			return threeTuples
					.descend((slot2, twoTuples) -> twoTuples.descend(
							(slot3, oneTuples) -> oneTuples.stream().map(slot4 -> create(first, slot2, slot3, slot4))))
					.iterator();
		}
		// wildcard everything
		return fourTuples
				.descend((slot1,
						threeTuples) -> threeTuples.descend((slot2, twoTuples) -> twoTuples.descend((slot3,
								oneTuples) -> oneTuples.stream().map(slot4 -> create(slot1, slot2, slot3, slot4)))))
				.iterator();
	}

	public abstract void add(Quad q);

	public void add(final Node first, final Node second, final Node third, final Node fourth) {
		log.debug("Adding four-tuple: {} {} {} {} .", first, second, third, fourth);
		FourTupleMap fourTuples = local.get();
		if (!fourTuples.containsKey(first)) fourTuples = fourTuples.plus(first, ThreeTupleMap.empty());

		ThreeTupleMap threeTuples = fourTuples.get(first);
		if (!threeTuples.containsKey(second)) threeTuples = threeTuples.plus(second, TwoTupleMap.empty());

		TwoTupleMap twoTuples = threeTuples.get(third);
		if (!twoTuples.containsKey(third)) twoTuples = twoTuples.plus(third, PersistentSet.empty());

		PersistentSet<Node> oneTuples = twoTuples.get(third);
		if (!oneTuples.contains(fourth)) oneTuples = oneTuples.plus(fourth);

		twoTuples = twoTuples.minus(third).plus(third, oneTuples);
		threeTuples = threeTuples.minus(second).plus(second, twoTuples);
		log.debug("Setting transactional index to new value.");
		local.set(fourTuples.minus(first).plus(first, threeTuples));
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

	public void commit() {
		// swap transactional reference in for shared reference
		master().set(local.get());
		end();
	}
}

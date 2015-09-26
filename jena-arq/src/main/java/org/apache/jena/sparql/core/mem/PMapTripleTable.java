package org.apache.jena.sparql.core.mem;

import static java.lang.ThreadLocal.withInitial;
import static java.util.stream.Stream.empty;
import static org.apache.jena.graph.Triple.create;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;
import org.apache.jena.sparql.core.mem.FourTupleMap.TwoTupleMap;
import org.slf4j.Logger;

public abstract class PMapTripleTable implements TripleTable {

	Logger log = getLogger(PMapTripleTable.class);

	AtomicReference<ThreeTupleMap> tmpIndex = new AtomicReference<>(ThreeTupleMap.empty());

	ThreadLocal<ThreeTupleMap> local = withInitial(() -> tmpIndex.get());

	ThreadLocal<Boolean> isInTransaction = withInitial(() -> false);

	private final String name;

	public PMapTripleTable(final String n) {
		this.name = n;
	}

	@Override
	public void clear() {
		local.set(ThreeTupleMap.empty());
	}

	@Override
	public void commit() {
		tmpIndex.set(local.get());
		end();
	}

	@Override
	public void abort() {
		end();
	}

	@Override
	public boolean isInTransaction() {
		return isInTransaction.get();
	}

	@Override
	public void end() {
		local.remove();
		isInTransaction.set(false);
	}

	private void debug(final String msg, final Object... values) {
		log.debug(name + ": " + msg, values);
	}

	public Stream<Triple> _find(final Node first, final Node second, final Node third) {
		debug("Querying on three-tuple pattern: {} {} {} .", first, second, third);
		final ThreeTupleMap threeTuples = local.get();

		if (first != null && first.isConcrete()) {
			log.debug("Using a specific first slot value.");
			if (!threeTuples.containsKey(first)) return empty();
			final TwoTupleMap twoTuples = threeTuples.get(first);
			if (second != null && second.isConcrete()) {
				log.debug("Using a specific second slot value.");
				if (!twoTuples.containsKey(second)) return empty();
				final PersistentSet<Node> oneTuples = twoTuples.get(second);
				if (third != null && third.isConcrete()) {
					log.debug("Using a specific third slot value.");
					if (!oneTuples.contains(third)) return empty();
					return Stream.of(create(first, second, third));
				}
				log.debug("Using a wildcard third slot value.");
				return oneTuples.stream().map(slot3 -> create(first, second, slot3));
			}
			log.debug("Using wildcard second and third slot values.");
			return twoTuples
					.descend((slot2, oneTuples) -> oneTuples.stream().map(slot3 -> create(first, slot2, slot3)));
		}
		log.debug("Using a wildcard for all slot values.");
		return threeTuples.descend((slot1, twoTuples) -> twoTuples
				.descend((slot2, oneTuples) -> oneTuples.stream().map(slot3 -> create(slot1, slot2, slot3))));
	}

	protected void _add(final Node first, final Node second, final Node third) {
		debug("Adding three-tuple {} {} {}", first, second, third);
		ThreeTupleMap threeTuples = local.get();

		if (!threeTuples.containsKey(first)) threeTuples = threeTuples.plus(first, TwoTupleMap.empty());

		TwoTupleMap twoTuples = threeTuples.get(first);
		if (!twoTuples.containsKey(second)) twoTuples = twoTuples.plus(second, PersistentSet.empty());

		PersistentSet<Node> oneTuples = twoTuples.get(second);
		if (!oneTuples.contains(third)) oneTuples = oneTuples.plus(third);

		twoTuples = twoTuples.minus(second).plus(second, oneTuples);
		local.set(threeTuples.minus(first).plus(first, twoTuples));
	}

	protected void _delete(final Node first, final Node second, final Node third) {
		debug("Deleting three-tuple {} {} {}", first, second, third);
		final ThreeTupleMap threeTuples = local.get();
		if (threeTuples.containsKey(first)) {
			TwoTupleMap twoTuples = threeTuples.get(first);
			if (twoTuples.containsKey(second)) {
				PersistentSet<Node> oneTuples = twoTuples.get(second);
				if (oneTuples.contains(third)) {
					oneTuples = oneTuples.minus(third);
					twoTuples = twoTuples.minus(second).plus(second, oneTuples);
					log.debug("Setting transactional index to new value.");
					local.set(threeTuples.minus(first).plus(first, twoTuples));
				}
			}
		}
	}

	@Override
	public void begin() {
		isInTransaction.set(true);
	}

}

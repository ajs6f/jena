/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.core.mem;

import static java.lang.ThreadLocal.withInitial;
import static java.util.Collections.emptyIterator;
import static org.apache.jena.atlas.iterator.Iter.singleton;
import static org.apache.jena.sparql.core.Quad.create;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.PMap;
import org.apache.jena.atlas.lib.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;
import org.apache.jena.sparql.core.mem.FourTupleMap.TwoTupleMap;
import org.slf4j.Logger;

/**
 * An implementation of {@link QuadTable} based on the use of nested {@link PMap}s. Intended for high-speed in-memory use.
 *
 */
public abstract class PMapBasedIndex implements QuadTable {

	private final String name;

	public PMapBasedIndex(final String n) {
		this.name = n;
	}

	private static final Logger log = getLogger(PMapBasedIndex.class);

	private void debug(final String msg, final Object... values) {
		log.debug(name + ": " + msg, values);
	}

	/**
	 * We use an {@link AtomicReference} to the internal {@link PMap} that holds our index data to be able to swap
	 * transactional versions of the index data with the shared version atomically.
	 */
	private final AtomicReference<FourTupleMap> index = new AtomicReference<>(FourTupleMap.empty());

	private AtomicReference<FourTupleMap> master() {
		return index;
	}

	private final ThreadLocal<FourTupleMap> local = withInitial(() -> master().get());

	private final ThreadLocal<Boolean> isInTransaction = withInitial(() -> false);

	protected ThreadLocal<FourTupleMap> local() {
		return local;
	}

	@Override
	public void begin(final ReadWrite rw) {
		begin();
	}

	@Override
	public void begin() {
		isInTransaction.set(true);
	}

	@Override
	public void abort() {
		end();
	}

	@Override
	public boolean isInTransaction() {
		return isInTransaction.get();
	};

	@Override
	public void end() {
		debug("Abandoning transactional reference.");
		local.remove();
		isInTransaction.set(false);
	}

	@Override
	public void commit() {
		debug("Swapping transactional reference in for shared reference");
		master().set(local.get());
		end();
	}

	/**
	 * We descend through the nested {@link PMap}s building up {@link Stream}s of partial tuples from which we develop a
	 * <code>Stream</code> of full tuples from which we get our {@link Iterator}. Use {@link Node#ANY} or
	 * <code>null</code> for a wildcard.
	 *
	 * @param first the value in the first slot of the tuple
	 * @param second the value in the first slot of the tuple
	 * @param third the value in the first slot of the tuple
	 * @param fourth the value in the first slot of the tuple
	 * @return an <code>Iterator</code> of tuples matching the pattern
	 */
	protected Iterator<Quad> _find(final Node first, final Node second, final Node third, final Node fourth) {
		debug("Querying on four-tuple pattern: {} {} {} {} .", first, second, third, fourth);
		final FourTupleMap fourTuples = local.get();
		if (first != null && first.isConcrete()) {
			log.debug("Using a specific first slot value.");
			if (!fourTuples.containsKey(first)) return emptyIterator();
			final ThreeTupleMap threeTuples = fourTuples.get(first);
			if (second != null && second.isConcrete()) {
				log.debug("Using a specific second slot value.");
				if (!threeTuples.containsKey(second)) return emptyIterator();
				final TwoTupleMap twoTuples = threeTuples.get(second);
				if (third != null && third.isConcrete()) {
					log.debug("Using a specific third slot value.");
					if (!twoTuples.containsKey(third)) return emptyIterator();
					final PersistentSet<Node> oneTuples = twoTuples.get(third);
					if (fourth != null && fourth.isConcrete()) {
						log.debug("Using a specific fourth slot value.");
						if (!oneTuples.contains(fourth)) return emptyIterator();
						return singleton(create(first, second, third, fourth));
					}
					log.debug("Using a wildcard fourth slot value.");
					return oneTuples.stream().map(slot4 -> create(first, second, third, slot4)).iterator();
				}
				log.debug("Using a wildcard third and fourth slot value.");
				return twoTuples.descend(
						(slot3, oneTuples) -> oneTuples.stream().map(slot4 -> create(first, second, slot3, slot4)))
						.iterator();
			}
			log.debug("Using a wildcard second, third and fourth slot value.");
			return threeTuples
					.descend((slot2, twoTuples) -> twoTuples.descend(
							(slot3, oneTuples) -> oneTuples.stream().map(slot4 -> create(first, slot2, slot3, slot4))))
					.iterator();
		}
		log.debug("Using a wildcard for all slot values.");
		return fourTuples
				.descend((slot1,
						threeTuples) -> threeTuples.descend((slot2, twoTuples) -> twoTuples.descend((slot3,
								oneTuples) -> oneTuples.stream().map(slot4 -> create(slot1, slot2, slot3, slot4)))))
				.iterator();
	}

	protected void _add(final Node first, final Node second, final Node third, final Node fourth) {
		debug("Adding four-tuple: {} {} {} {} .", first, second, third, fourth);
		FourTupleMap fourTuples = local.get();
		if (!fourTuples.containsKey(first)) fourTuples = fourTuples.plus(first, ThreeTupleMap.empty());

		ThreeTupleMap threeTuples = fourTuples.get(first);
		if (!threeTuples.containsKey(second)) threeTuples = threeTuples.plus(second, TwoTupleMap.empty());

		TwoTupleMap twoTuples = threeTuples.get(second);
		if (!twoTuples.containsKey(third)) twoTuples = twoTuples.plus(third, PersistentSet.empty());

		PersistentSet<Node> oneTuples = twoTuples.get(third);
		if (!oneTuples.contains(fourth)) oneTuples = oneTuples.plus(fourth);

		twoTuples = twoTuples.minus(third).plus(third, oneTuples);
		threeTuples = threeTuples.minus(second).plus(second, twoTuples);
		debug("Setting transactional index to new value.");
		local.set(fourTuples.minus(first).plus(first, threeTuples));
	}

	protected void _delete(final Node first, final Node second, final Node third, final Node fourth) {
		debug("Removing four-tuple: {} {} {} {} .", first, second, third, fourth);
		final FourTupleMap fourTuples = local.get();
		if (fourTuples.containsKey(first)) {
			ThreeTupleMap threeTuples = fourTuples.get(first);
			if (threeTuples.containsKey(second)) {
				TwoTupleMap twoTuples = threeTuples.get(second);
				if (twoTuples.containsKey(third)) {
					PersistentSet<Node> oneTuples = twoTuples.get(third);
					if (oneTuples.contains(fourth)) {
						oneTuples = oneTuples.minus(fourth);
						twoTuples = twoTuples.minus(third).plus(third, oneTuples);
						threeTuples = threeTuples.minus(second).plus(second, twoTuples);
						debug("Setting transactional index to new value.");
						local.set(fourTuples.minus(first).plus(first, threeTuples));
					}
				}
			}
		}
	}
}

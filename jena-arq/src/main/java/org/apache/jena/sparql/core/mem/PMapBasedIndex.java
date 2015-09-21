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

import org.apache.jena.atlas.lib.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;
import org.apache.jena.sparql.core.mem.FourTupleMap.TwoTupleMap;
import org.slf4j.Logger;

public abstract class PMapBasedIndex implements Index {

	private final String name;

	public PMapBasedIndex(final String n) {
		this.name = n;
	}

	private static final Logger log = getLogger(PMapBasedIndex.class);

	private void debug(final String msg, final Object... values) {
		log.debug(name + ": " + msg, values);
	}

	private final AtomicReference<FourTupleMap> index = new AtomicReference<>(FourTupleMap.empty());

	private AtomicReference<FourTupleMap> master() {
		return index;
	}

	ThreadLocal<FourTupleMap> local;

	public void begin() {
		debug("Capturing transactional reference.");
		local = withInitial(() -> master().get());
	}

	public void end() {
		debug("Abandoning transactional reference.");
		local = null;
	}

	protected Iterator<Quad> _find(final Node first, final Node second, final Node third, final Node fourth) {
		debug("Querying on four-tuple pattern: {} {} {} {} .", first, second, third, fourth);
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

	void _add(final Node first, final Node second, final Node third, final Node fourth) {
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

	void _delete(final Node first, final Node second, final Node third, final Node fourth) {
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
						local.set(fourTuples.minus(first).plus(first, threeTuples));
					}
				}
			}
		}
	}

	public void commit() {
		debug("Swapping transactional reference in for shared reference");
		master().set(local.get());
		end();
	}
}

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

import static java.util.stream.Stream.empty;
import static org.apache.jena.sparql.core.Quad.create;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.stream.Stream;

import org.apache.jena.atlas.lib.PMap;
import org.apache.jena.atlas.lib.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;
import org.apache.jena.sparql.core.mem.FourTupleMap.TwoTupleMap;
import org.slf4j.Logger;

/**
 * An implementation of {@link QuadTable} based on the use of nested {@link PMap}s. Intended for high-speed in-memory
 * use.
 *
 */
public abstract class PMapQuadTable extends PMapTupleTable<FourTupleMap, Quad>implements QuadTable {

	public PMapQuadTable(final String tableName) {
		super(tableName);
	}

	private static final Logger log = getLogger(PMapQuadTable.class);

	@Override
	protected Logger log() {
		return log;
	}

	@Override
	protected FourTupleMap initial() {
		return FourTupleMap.empty();
	}

	/**
	 * We descend through the nested {@link PMap}s building up {@link Stream}s of partial tuples from which we develop a
	 * {@link Stream} of full tuples which is our result. Use {@link Node#ANY} or <code>null</code> for a wildcard.
	 *
	 * @param first the value in the first slot of the tuple
	 * @param second the value in the second slot of the tuple
	 * @param third the value in the third slot of the tuple
	 * @param fourth the value in the fourth slot of the tuple
	 * @return a <code>Stream</code> of tuples matching the pattern
	 */
	protected Stream<Quad> _find(final Node first, final Node second, final Node third, final Node fourth) {
		debug("Querying on four-tuple pattern: {} {} {} {} .", first, second, third, fourth);
		final FourTupleMap fourTuples = local().get();
		if (first != null && first.isConcrete()) {
			debug("Using a specific first slot value.");
			if (!fourTuples.containsKey(first)) return empty();
			final ThreeTupleMap threeTuples = fourTuples.get(first);
			if (second != null && second.isConcrete()) {
				debug("Using a specific second slot value.");
				if (!threeTuples.containsKey(second)) return empty();
				final TwoTupleMap twoTuples = threeTuples.get(second);
				if (third != null && third.isConcrete()) {
					debug("Using a specific third slot value.");
					if (!twoTuples.containsKey(third)) return empty();
					final PersistentSet<Node> oneTuples = twoTuples.get(third);
					if (fourth != null && fourth.isConcrete()) {
						debug("Using a specific fourth slot value.");
						if (!oneTuples.contains(fourth)) return empty();
						return Stream.of(create(first, second, third, fourth));
					}
					debug("Using a wildcard fourth slot value.");
					return oneTuples.stream().map(slot4 -> create(first, second, third, slot4));
				}
				debug("Using wildcard third and fourth slot values.");
				return twoTuples.descend(
						(slot3, oneTuples) -> oneTuples.stream().map(slot4 -> create(first, second, slot3, slot4)));
			}
			debug("Using wildcard second, third and fourth slot values.");
			return threeTuples.descend((slot2, twoTuples) -> twoTuples.descend(
					(slot3, oneTuples) -> oneTuples.stream().map(slot4 -> create(first, slot2, slot3, slot4))));
		}
		debug("Using a wildcard for all slot values.");
		return fourTuples.descend((slot1, threeTuples) -> threeTuples.descend((slot2, twoTuples) -> twoTuples
				.descend((slot3, oneTuples) -> oneTuples.stream().map(slot4 -> create(slot1, slot2, slot3, slot4)))));
	}

	protected void _add(final Node first, final Node second, final Node third, final Node fourth) {
		debug("Adding four-tuple: {} {} {} {} .", first, second, third, fourth);
		final FourTupleMap fourTuples = local().get();

		ThreeTupleMap threeTuples = fourTuples.containsKey(first) ? fourTuples.get(first) : ThreeTupleMap.empty();
		TwoTupleMap twoTuples = threeTuples.containsKey(second) ? threeTuples.get(second) : TwoTupleMap.empty();
		PersistentSet<Node> oneTuples = twoTuples.containsKey(third)? twoTuples.get(third) : PersistentSet.empty();

		oneTuples = oneTuples.plus(fourth);
		twoTuples = twoTuples.minus(third).plus(third, oneTuples);
		threeTuples = threeTuples.minus(second).plus(second, twoTuples);
		debug("Setting transactional index to new value.");
		local().set(fourTuples.minus(first).plus(first, threeTuples));
	}

	protected void _delete(final Node first, final Node second, final Node third, final Node fourth) {
		debug("Removing four-tuple: {} {} {} {} .", first, second, third, fourth);
		final FourTupleMap fourTuples = local().get();
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
						local().set(fourTuples.minus(first).plus(first, threeTuples));
					}
				}
			}
		}
	}
}

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
import static org.apache.jena.graph.Triple.create;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.stream.Stream;

import org.apache.jena.atlas.lib.PMap;
import org.apache.jena.atlas.lib.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;
import org.apache.jena.sparql.core.mem.FourTupleMap.TwoTupleMap;
import org.slf4j.Logger;

/**
 * A {@link TripleTable} employing persistent maps to index triples in one particular slot order (e.g. SPO, OSP or POS).
 *
 */
public abstract class PMapTripleTable extends PMapTupleTable<ThreeTupleMap, Triple>implements TripleTable {

	private final static Logger log = getLogger(PMapTripleTable.class);

	@Override
	protected Logger log() {
		return log;
	}

	@Override
	protected ThreeTupleMap initial() {
		return ThreeTupleMap.empty();
	}

	public PMapTripleTable(final String tableName) {
		super(tableName);
	}

	/**
	 * We descend through the nested {@link PMap}s building up {@link Stream}s of partial tuples from which we develop a
	 * {@link Stream} of full tuples which is our result. Use {@link Node#ANY} or <code>null</code> for a wildcard.
	 *
	 * @param first the value in the first slot of the tuple
	 * @param second the value in the second slot of the tuple
	 * @param third the value in the third slot of the tuple
	 * @return a <code>Stream</code> of tuples matching the pattern
	 */
	public Stream<Triple> _find(final Node first, final Node second, final Node third) {
		debug("Querying on three-tuple pattern: {} {} {} .", first, second, third);
		final ThreeTupleMap threeTuples = local().get();

		if (first != null && first.isConcrete()) {
			debug("Using a specific first slot value.");
			if (!threeTuples.containsKey(first)) return empty();
			final TwoTupleMap twoTuples = threeTuples.get(first);
			if (second != null && second.isConcrete()) {
				debug("Using a specific second slot value.");
				if (!twoTuples.containsKey(second)) return empty();
				final PersistentSet<Node> oneTuples = twoTuples.get(second);
				if (third != null && third.isConcrete()) {
					debug("Using a specific third slot value.");
					if (!oneTuples.contains(third)) return empty();
					return Stream.of(create(first, second, third));
				}
				debug("Using a wildcard third slot value.");
				return oneTuples.stream().map(slot3 -> create(first, second, slot3));
			}
			debug("Using wildcard second and third slot values.");
			return twoTuples
					.descend((slot2, oneTuples) -> oneTuples.stream().map(slot3 -> create(first, slot2, slot3)));
		}
		debug("Using a wildcard for all slot values.");
		return threeTuples.descend((slot1, twoTuples) -> twoTuples
				.descend((slot2, oneTuples) -> oneTuples.stream().map(slot3 -> create(slot1, slot2, slot3))));
	}

	protected void _add(final Node first, final Node second, final Node third) {
		debug("Adding three-tuple {} {} {}", first, second, third);
		final ThreeTupleMap threeTuples = local().get();
		TwoTupleMap twoTuples = threeTuples.containsKey(first) ? threeTuples.get(first) : TwoTupleMap.empty();
		PersistentSet<Node> oneTuples = twoTuples.containsKey(second) ? twoTuples.get(second) : PersistentSet.empty();

		oneTuples = oneTuples.plus(third);
		twoTuples = twoTuples.minus(second).plus(second, oneTuples);
		local().set(threeTuples.minus(first).plus(first, twoTuples));
	}

	protected void _delete(final Node first, final Node second, final Node third) {
		debug("Deleting three-tuple {} {} {}", first, second, third);
		final ThreeTupleMap threeTuples = local().get();
		if (threeTuples.containsKey(first)) {
			TwoTupleMap twoTuples = threeTuples.get(first);
			if (twoTuples.containsKey(second)) {
				PersistentSet<Node> oneTuples = twoTuples.get(second);
				if (oneTuples.contains(third)) {
					oneTuples = oneTuples.minus(third);
					twoTuples = twoTuples.minus(second).plus(second, oneTuples);
					debug("Setting transactional index to new value.");
					local().set(threeTuples.minus(first).plus(first, twoTuples));
				}
			}
		}
	}
}

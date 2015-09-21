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

import static java.util.EnumSet.noneOf;
import static java.util.stream.Collectors.toMap;
import static org.apache.jena.sparql.core.mem.IndexForm.*;
import static org.apache.jena.sparql.core.mem.IndexForm.Slot.*;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.IndexForm.Slot;

public class HexIndex extends Index {

	public HexIndex() {
		super("HexIndex");
	}

	private final Map<IndexForm, Index> indexBlock = new EnumMap<IndexForm, Index>(
			indexForms().collect(toMap(x -> x, IndexForm::get)));

	@Override
	public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o) {
		final Set<Slot> pattern = noneOf(IndexForm.Slot.class);
		if (isConcrete(g)) pattern.add(GRAPH);
		if (isConcrete(s)) pattern.add(SUBJECT);
		if (isConcrete(p)) pattern.add(PREDICATE);
		if (isConcrete(o)) pattern.add(OBJECT);
		final IndexForm choice = chooseFrom(pattern);
		return indexBlock.get(choice).find(g, s, p, o);
	}

	private static boolean isConcrete(final Node n) {
		return n != null && n.isConcrete();
	}

	@Override
	public void add(final Quad q) {
		indexBlock.values().forEach(index -> index.add(q));
	}

	@Override
	public void delete(final Quad q) {
		indexBlock.values().forEach(index -> index.delete(q));
	}

	Iterator<Node> listGraphNodes() {
		return indexBlock.get(GSPO).local.get().entrySet().stream().map(Map.Entry::getKey).iterator();
	}

	@Override
	public void begin() {
		indexBlock.values().forEach(Index::begin);
	}

	@Override
	public void end() {
		indexBlock.values().forEach(Index::end);
	}

	@Override
	public void commit() {
		indexBlock.values().forEach(Index::commit);
	}
}

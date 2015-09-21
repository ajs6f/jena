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

import static java.util.EnumSet.allOf;
import static org.apache.jena.ext.com.google.common.collect.ImmutableSet.of;
import static org.apache.jena.ext.com.google.common.collect.Sets.powerSet;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.graph.NodeFactory.createURI;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.IndexForm.Slot;
import org.junit.Assert;

public abstract class IndexTest extends Assert {

	protected static final Node sampleNode = createURI("info:test");
	protected static final Quad q = Quad.create(sampleNode, sampleNode, sampleNode, sampleNode);

	protected static Stream<Set<Slot>> queryPatterns() {
		return powerSet(allOf(Slot.class)).stream();
	}

	protected static final Set<Slot> allWildcardQuery = of();

	public void addAndRemoveSomeQuads(final PMapBasedIndex index) {

		// simple add-and-delete
		index.begin();
		index.add(q);
		Iterator<Quad> contents = index.find(ANY, ANY, ANY, ANY);
		assertTrue(contents.hasNext());
		assertEquals(q, contents.next());
		assertFalse(contents.hasNext());
		index.delete(q);
		contents = index.find(ANY, ANY, ANY, ANY);
		assertFalse(contents.hasNext());
		index.end();

		// add, abort, then check to see that nothing was persisted
		index.begin();
		index.add(q);
		contents = index.find(ANY, ANY, ANY, ANY);
		assertTrue(contents.hasNext());
		assertEquals(q, contents.next());
		assertFalse(contents.hasNext());
		index.end();
		index.begin();
		contents = index.find(ANY, ANY, ANY, ANY);
		assertFalse(contents.hasNext());
		index.end();

		// add, commit, and check to see that persistence occurred
		index.begin();
		index.add(q);
		contents = index.find(ANY, ANY, ANY, ANY);
		assertTrue(contents.hasNext());
		assertEquals(q, contents.next());
		assertFalse(contents.hasNext());
		index.commit();
		index.begin();
		contents = index.find(ANY, ANY, ANY, ANY);
		assertTrue(contents.hasNext());
		assertEquals(q, contents.next());
		assertFalse(contents.hasNext());
		index.end();
	}
}

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
import static java.util.stream.Collectors.toSet;
import static org.apache.jena.ext.com.google.common.collect.ImmutableSet.of;
import static org.apache.jena.ext.com.google.common.collect.Sets.powerSet;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;

import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.junit.Assert;

public abstract class QuadTableTest extends Assert {

	protected static final Node sampleNode = createURI("info:test");
	protected static final Quad q = Quad.create(sampleNode, sampleNode, sampleNode, sampleNode);

	protected static Stream<Set<Slot>> queryPatterns() {
		return powerSet(allOf(Slot.class)).stream();
	}

	protected static final Set<Slot> allWildcardQuery = of();

	public void addAndRemoveSomeQuads(final QuadTable index) {

		// simple add-and-delete
		index.begin(WRITE);
		index.add(q);
		Set<Quad> contents = index.find(ANY, ANY, ANY, ANY).collect(toSet());
		assertEquals(ImmutableSet.of(q), contents);
		index.delete(q);
		contents = index.find(ANY, ANY, ANY, ANY).collect(toSet());
		assertTrue(contents.isEmpty());
		index.end();

		// add, abort, then check to see that nothing was persisted
		index.begin(WRITE);
		index.add(q);
		contents = index.find(ANY, ANY, ANY, ANY).collect(toSet());
		assertEquals(ImmutableSet.of(q), contents);
		index.end();
		index.begin(READ);
		contents = index.find(ANY, ANY, ANY, ANY).collect(toSet());
		assertTrue(contents.isEmpty());
		index.end();

		// add, commit, and check to see that persistence occurred
		index.begin(WRITE);
		index.add(q);
		contents = index.find(ANY, ANY, ANY, ANY).collect(toSet());
		assertEquals(ImmutableSet.of(q), contents);
		index.commit();
		index.begin(READ);
		contents = index.find(ANY, ANY, ANY, ANY).collect(toSet());
		assertEquals(ImmutableSet.of(q), contents);
		index.end();
	}
}

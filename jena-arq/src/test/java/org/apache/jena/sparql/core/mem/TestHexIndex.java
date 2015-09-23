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

import static org.apache.jena.atlas.iterator.Iter.toSet;
import static org.apache.jena.ext.com.google.common.collect.ImmutableSet.of;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.sparql.core.mem.IndexForm.Slot.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.IndexForm.Slot;
import org.junit.Test;

public class TestHexIndex extends IndexTest {

	@Test
	public void addAndRemoveSomeQuads() {
		addAndRemoveSomeQuads(new HexIndex());
	}

	@Test
	public void testListGraphNodes() {
		final int nodesToTry = 50;
		final HexIndex index = new HexIndex();
		final Set<Node> graphNodes = new HashSet<>();
		index.begin(null);
		for (int i = 0; i < nodesToTry; i++) {
			final Node node = createBlankNode();
			index.add(Quad.create(node, node, node, node));
			graphNodes.add(node);
			assertEquals(graphNodes, toSet(index.listGraphNodes()));
		}
		index.end();
	}

	@Test
	public void checkConcreteQueries() {
		queryPatterns().filter(p -> !allWildcardQuery.equals(p)).map(TestHexIndex::exampleFrom).forEach(testQuery -> {
			final HexIndex index = new HexIndex();
			index.begin(null);
			// add our sample quad
			index.add(q);
			// add a noise quad from which our sample should be distinguished
			final Node node = createBlankNode();
			final Quad noiseQuad = Quad.create(node, node, node, node);
			index.add(noiseQuad);
			index.commit();

			index.begin(null);
			Iterator<Quad> contents = index.find(testQuery.getGraph(), testQuery.getSubject(), testQuery.getPredicate(),
					testQuery.getObject());
			assertTrue(contents.hasNext());
			assertEquals(q, contents.next());
			assertFalse(contents.hasNext());
			// both Node.ANY and null should work as wildcards
			contents = index.find(null, ANY, null, ANY);
			assertEquals(of(q, noiseQuad), toSet(contents));
			index.end();
		});
	}

	private static Quad exampleFrom(final Set<Slot> pattern) {
		return Quad.create(pattern.contains(GRAPH) ? sampleNode : ANY, pattern.contains(SUBJECT) ? sampleNode : ANY,
				pattern.contains(PREDICATE) ? sampleNode : ANY, pattern.contains(OBJECT) ? sampleNode : ANY);
	}
}

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

import static org.apache.jena.atlas.iterator.Iter.distinct;
import static org.apache.jena.atlas.iterator.Iter.map;
import static org.apache.jena.graph.Node.ANY;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;

/**
 * A simplex or multiplex index of {@link Quad}s. Implementations may wish to override {@link #listGraphNodes()} with a
 * more efficient implementation.
 *
 */
public interface Index extends Transactional {

	/**
	 * Search the index using a pattern of slots. {@link Node#ANY} or <code>null</code> will work as a wildcard.
	 *
	 * @param g the graph node of the pattern
	 * @param s the subject node of the pattern
	 * @param p the predicate node of the pattern
	 * @param o the object node of the pattern
	 * @return an {@link Iterator} of matched quads
	 */
	Iterator<Quad> find(Node g, Node s, Node p, Node o);

	/**
	 * Add a {@link Quad} to the index
	 *
	 * @param q the quad to add
	 */
	void add(Quad q);

	/**
	 * Remove a {@link Quad} from the index
	 *
	 * @param q the quad to remove
	 */
	void delete(Quad q);

	/**
	 * Discover the graphs named in the index
	 *
	 * @return an {@link Iterator} of graph names used in this index
	 */
	default Iterator<Node> listGraphNodes() {
		return distinct(map(find(ANY, ANY, ANY, ANY), Quad::getGraph));
	}
}

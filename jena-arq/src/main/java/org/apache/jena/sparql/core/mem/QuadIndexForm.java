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

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.iterate;
import static org.apache.jena.sparql.core.mem.Slot.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 * Six covering index forms and machinery to determine which of them is best suited to answer a given query.
 *
 */
public enum QuadIndexForm implements Supplier<QuadTable> {

	GSPO(asList(GRAPH, SUBJECT, PREDICATE, OBJECT)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {
				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(g, s, p, o);
				}

				@Override
				public void add(final Quad q) {
					_add(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject());
				}

				@Override
				public Stream<Node> listGraphNodes() {
					return local().get().entryStream().map(Entry::getKey);
				}
			};
		}
	},
	GOPS(asList(GRAPH, OBJECT, PREDICATE, SUBJECT)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {

				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(g, o, p, s);
				}

				@Override
				public void add(final Quad q) {
					_add(q.getGraph(), q.getObject(), q.getPredicate(), q.getSubject());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getGraph(), q.getObject(), q.getPredicate(), q.getSubject());
				}
			};

		}
	},
	SPOG(asList(SUBJECT, PREDICATE, OBJECT, GRAPH)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {

				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(s, p, o, g);
				}

				@Override
				public void add(final Quad q) {
					_add(q.getSubject(), q.getPredicate(), q.getObject(), q.getGraph());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getSubject(), q.getPredicate(), q.getObject(), q.getGraph());
				}
			};
		}
	},
	OSGP(asList(OBJECT, SUBJECT, GRAPH, PREDICATE)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {

				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(o, s, g, p);
				}

				@Override
				public void add(final Quad q) {
					_add(q.getObject(), q.getSubject(), q.getGraph(), q.getPredicate());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getObject(), q.getSubject(), q.getGraph(), q.getPredicate());
				}
			};
		}
	},
	PGSO(asList(PREDICATE, GRAPH, SUBJECT, OBJECT)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {

				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(p, g, s, o);
				}

				@Override
				public void add(final Quad q) {
					_add(q.getPredicate(), q.getGraph(), q.getSubject(), q.getObject());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getPredicate(), q.getGraph(), q.getSubject(), q.getObject());
				}
			};
		}
	},
	OPSG(asList(OBJECT, PREDICATE, SUBJECT, GRAPH)) {
		@Override
		public PMapQuadTable get() {
			return new PMapQuadTable(name()) {

				@Override
				public Stream<Quad> find(final Node g, final Node s, final Node p, final Node o) {
					return _find(o, p, s, g);
				}

				@Override
				public void add(final Quad q) {
					_add(q.getObject(), q.getPredicate(), q.getSubject(), q.getGraph());
				}

				@Override
				public void delete(final Quad q) {
					_delete(q.getObject(), q.getPredicate(), q.getSubject(), q.getGraph());
				}
			};
		}
	};

	private QuadIndexForm(final List<Slot> fp) {
		this.fullpattern = fp;
	}

	public final List<Slot> fullpattern;

	public boolean avoidsTraversal(final Set<Slot> pattern) {
		return iterate(4, i -> i - 1).limit(4).map(j -> fullpattern.subList(0, j)).map(EnumSet::copyOf)
				.anyMatch(pattern::equals);
	}

	public static QuadIndexForm chooseFrom(final Set<Slot> pattern) {
		return indexForms().filter(f -> f.avoidsTraversal(pattern)).findFirst().orElse(GSPO);
	}

	public static Stream<QuadIndexForm> indexForms() {
		return stream(values());
	}
}

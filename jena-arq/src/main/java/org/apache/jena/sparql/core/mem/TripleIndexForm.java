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

import static java.util.Arrays.stream;
import static java.util.EnumSet.of;
import static org.apache.jena.sparql.core.mem.Slot.*;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public enum TripleIndexForm implements Supplier<TripleTable> {

	SPO(of(SUBJECT, PREDICATE), SUBJECT) {
		@Override
		public TripleTable get() {
			return new PMapTripleTable(name()) {

				@Override
				public Stream<Triple> find(final Node s, final Node p, final Node o) {
					return _find(s, p, o);
				}

				@Override
				public void add(final Triple t) {
					_add(t.getSubject(), t.getPredicate(), t.getObject());
				}

				@Override
				public void delete(final Triple t) {
					_delete(t.getSubject(), t.getPredicate(), t.getObject());
				}

			};
		}

	},
	POS(of(PREDICATE, OBJECT), PREDICATE) {

		@Override
		public TripleTable get() {
			return new PMapTripleTable(name()) {

				@Override
				public Stream<Triple> find(final Node s, final Node p, final Node o) {
					return _find(p, o, s);
				}

				@Override
				public void add(final Triple t) {
					_add(t.getPredicate(), t.getObject(), t.getSubject());
				}

				@Override
				public void delete(final Triple t) {
					_delete(t.getPredicate(), t.getObject(), t.getSubject());
				}

			};
		}
	},
	OSP(of(OBJECT, SUBJECT), OBJECT) {

		@Override
		public TripleTable get() {
			return new PMapTripleTable(name()) {

				@Override
				public Stream<Triple> find(final Node s, final Node p, final Node o) {
					return _find(o, s, p);
				}

				@Override
				public void add(final Triple t) {
					_add(t.getObject(), t.getSubject(), t.getPredicate());
				}

				@Override
				public void delete(final Triple t) {
					_delete(t.getObject(), t.getSubject(), t.getPredicate());
				}

			};
		}
	};
	private TripleIndexForm(final Set<Slot> tp, final Slot op) {
		this.twoPrefix = tp;
		this.onePrefix = of(op);
	}

	public final Set<Slot> twoPrefix, onePrefix;

	public boolean avoidsTraversal(final Set<Slot> pattern) {
		return twoPrefix.equals(pattern) || onePrefix.equals(pattern);
	}

	public static TripleIndexForm chooseFrom(final Set<Slot> pattern) {
		return indexForms().filter(f -> f.avoidsTraversal(pattern)).findFirst().orElse(SPO);
	}

	public static Stream<TripleIndexForm> indexForms() {
		return stream(values());
	}
}

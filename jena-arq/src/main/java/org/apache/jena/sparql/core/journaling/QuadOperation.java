/**
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

package org.apache.jena.sparql.core.journaling;

import static java.util.Objects.hash;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.journaling.Operation.InvertibleOperation;

/**
 * @author ajs6f
 *
 */
public abstract class QuadOperation<SelfType extends QuadOperation<SelfType, InverseType>, InverseType extends QuadOperation<InverseType, SelfType>>
		extends Quad implements InvertibleOperation<Quad, DatasetGraph, SelfType, InverseType> {

	public QuadOperation(final Quad q) {
		super(q.getGraph(), q.asTriple());
	}

	public static class QuadAddition extends QuadOperation<QuadAddition, QuadDeletion> {

		public QuadAddition(final Quad q) {
			super(q);
		}

		@Override
		public Quad data() {
			return this;
		}

		@Override
		public QuadDeletion inverse() {
			return new QuadDeletion(this);
		}

		@Override
		public void actOn(final DatasetGraph dsg) {
			dsg.add(this);
		}

		@Override
		public String toString() {
			return "ADD " + super.toString();
		}

		@Override
		public int hashCode() {
			return hash(data());
		}

		@Override
		public boolean equals(final Object other) {
			if (other instanceof QuadAddition) return super.equals(((QuadAddition) other).data());
			return false;
		}
	}

	public static class QuadDeletion extends QuadOperation<QuadDeletion, QuadAddition> {

		public QuadDeletion(final Quad q) {
			super(q);
		}

		@Override
		public Quad data() {
			return this;
		}

		@Override
		public QuadAddition inverse() {
			return new QuadAddition(this);
		}

		@Override
		public void actOn(final DatasetGraph dsg) {
			dsg.delete(this);
		}

		@Override
		public String toString() {
			return "DELETE " + super.toString();
		}

		@Override
		public int hashCode() {
			return hash(data());
		}

		@Override
		public boolean equals(final Object other) {
			if (other instanceof QuadDeletion) return super.equals(((QuadDeletion) other).data());
			return false;
		}
	}
}

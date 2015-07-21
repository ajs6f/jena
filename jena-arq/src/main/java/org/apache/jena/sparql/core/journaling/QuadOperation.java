/**
 *
 */
package org.apache.jena.sparql.core.journaling;

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
		public QuadAddition data() {
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
	}

	public static class QuadDeletion extends QuadOperation<QuadDeletion, QuadAddition> {

		public QuadDeletion(final Quad q) {
			super(q);
		}

		@Override
		public QuadDeletion data() {
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
	}
}

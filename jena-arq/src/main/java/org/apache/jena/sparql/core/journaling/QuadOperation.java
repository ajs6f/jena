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
public abstract class QuadOperation extends Quad implements InvertibleOperation<Quad, DatasetGraph> {

	public QuadOperation(final Quad q) {
		super(q.getGraph(), q.asTriple());
	}

	public static class QuadAddition extends QuadOperation {

		public QuadAddition(final Quad q) {
			super(q);
		}

		@Override
		public Quad data() {
			return this;
		}

		@Override
		public InvertibleOperation<Quad, DatasetGraph> inverse() {
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

	public static class QuadDeletion extends QuadOperation {

		public QuadDeletion(final Quad q) {
			super(q);
		}

		@Override
		public Quad data() {
			return this;
		}

		@Override
		public InvertibleOperation<Quad, DatasetGraph> inverse() {
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

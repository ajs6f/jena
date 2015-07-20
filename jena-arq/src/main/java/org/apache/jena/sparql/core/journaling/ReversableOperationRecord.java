package org.apache.jena.sparql.core.journaling;

import org.apache.jena.sparql.core.journaling.Operation.InvertibleOperation;

/**
 * A reversable {@link OperationRecord} containing invertible operations.
 */
public interface ReversableOperationRecord<OpType extends InvertibleOperation<?, ?>> extends OperationRecord<OpType> {

	/**
	 * Produces a reversed version of this record with each operation inverted. If this record is run against a service,
	 * and then its reverse is run against a service, no change should result in the state of that service. This method
	 * may not produce an independent object: in other words, it may not be possible to use {@link #reverse()} on the
	 * result of this method to recover the original.
	 *
	 * @return the inverted reverse of this record
	 */
	ReversableOperationRecord<OpType> reverse();
}

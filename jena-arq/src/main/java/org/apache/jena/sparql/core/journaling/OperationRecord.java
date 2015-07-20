package org.apache.jena.sparql.core.journaling;

import java.util.function.Consumer;

import org.apache.jena.sparql.core.journaling.Operation.InvertibleOperation;

/**
 * A record of a series of operations with some facilities for use with transactional code.
 *
 * @param <OpType> the type of operation recorded
 */
public interface OperationRecord<OpType extends Operation<?, ?>> {

	/**
	 * Add an operation to the end of the record.
	 *
	 * @param op the operation to add
	 * @return whether or not the addition was successful
	 */
	void add(OpType op);

	/**
	 * For each {@link Operation} in this record from the least-recently added to the most-recently added, use
	 * <code>consumer::accept</code> with that operation and then discard that operation from the record. For example,
	 * an implementation backed by a {@link List} might use
	 * <code>list.forEach(op -> {consumer.accept(op); remove(op);});</code>.
	 *
	 * @param service
	 */
	void consume(Consumer<OpType> consumer);

	/**
	 * Clear this record.
	 */
	default void clear() {
		consume(op -> { // /dev/null
		});
	}

	/**
	 * A reversible {@link OperationRecord} containing invertible operations.
	 */
	public static interface ReversibleOperationRecord<OpType extends InvertibleOperation<?, ?>>
			extends OperationRecord<OpType> {

		/**
		 * Produces a time-reversed version of this record. If a record is compact, which means that every operation in
		 * it caused a definite change in the state of the service against which it was run, and this record is run
		 * against a service, and then its reverse is run against a service with each operation inverted, no change
		 * should result in the state of that service. This method may not produce an independent object: in other
		 * words, it may not be possible to use {@link #reverse()} on the result of this method to recover the original.
		 *
		 * @return the reverse of this record
		 */
		ReversibleOperationRecord<OpType> reverse();
	}
}

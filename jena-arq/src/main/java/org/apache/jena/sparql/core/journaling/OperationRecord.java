package org.apache.jena.sparql.core.journaling;

import java.util.function.Consumer;

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
	 */
	void add(OpType op);

	/**
	 * For each {@link Operation} from the least-recently added to the most-recently added, use
	 * {@link Consumer#accept(Object)} with that operation and then discard that operation. For example, an
	 * implementation is backed by a {@link List} might use
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
}

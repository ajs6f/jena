package org.apache.jena.sparql.core.journaling;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.sparql.core.journaling.Operation.InvertibleOperation;

public class ListBackedOperationRecord<OpType extends InvertibleOperation<?, ?>>
		implements ReversableOperationRecord<OpType> {

	/**
	 * The {@link List} in which we will keep a record of operations.
	 */
	private final List<OpType> operations;

	/**
	 * @param record the list to use in recording operations
	 */
	public ListBackedOperationRecord(final List<OpType> record) {
		this.operations = record;
	}

	@Override
	public ListBackedOperationRecord<OpType> reverse() {
		Collections.reverse(operations);
		return this;
	}

	@Override
	public void clear() {
		operations.clear();
	}

	@Override
	public void consume(final Consumer<OpType> consumer) {
		operations.forEach(op -> {
			consumer.accept(op);
			operations.remove(op);
		});
	}

	@Override
	public void add(final OpType op) {
		operations.add(op);
	}
}

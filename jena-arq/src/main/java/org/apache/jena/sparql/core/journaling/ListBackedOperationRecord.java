package org.apache.jena.sparql.core.journaling;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.sparql.core.journaling.Operation.InvertibleOperation;
import org.apache.jena.sparql.core.journaling.OperationRecord.ReversibleOperationRecord;

public class ListBackedOperationRecord<OpType extends InvertibleOperation<?, ?>>
		implements ReversibleOperationRecord<OpType> {

	private final List<OpType> operations;

	public ListBackedOperationRecord(final List<OpType> ops) {
		operations = ops;
	}

	@Override
	public void add(final OpType op) {
		operations.add(op);
	}

	@Override
	public ListBackedOperationRecord<OpType> reverse() {
		return new ListBackedOperationRecord<>(Lists.reverse(operations));
	}

	@Override
	public void consume(final Consumer<OpType> consumer) {
		final Iterator<OpType> i = operations.iterator();
		while (i.hasNext()) {
			consumer.accept(i.next());
			i.remove();
		}
	}

	@Override
	public void clear() {
		operations.clear();
	}
}

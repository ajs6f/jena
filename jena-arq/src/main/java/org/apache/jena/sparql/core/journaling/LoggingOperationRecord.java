package org.apache.jena.sparql.core.journaling;

import java.util.function.Consumer;

import org.slf4j.Logger;

public class LoggingOperationRecord implements OperationRecord<QuadOperation> {

	private final Logger log;

	/**
	 * @param log logger to which to log operations
	 */
	public LoggingOperationRecord(final Logger log) {
		this.log = log;
	}

	@Override
	public void add(final QuadOperation op) {
		log.info(op.toString());
	}

	@Override
	public void consume(final Consumer<QuadOperation> consumer) {
		throw new UnsupportedOperationException("LoggingOperationRecord is write-only!");
	}
}

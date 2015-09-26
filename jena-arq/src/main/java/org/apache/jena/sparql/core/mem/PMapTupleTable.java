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

import static java.lang.ThreadLocal.withInitial;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.query.ReadWrite;
import org.slf4j.Logger;

/**
 * A partial implementation of {@link TupleTable} that contains some state management.
 *
 * @param <TupleMapType> the type of the internal structure holding index data
 * @param <TupleType> the type of tuple in which a subclass of this class transacts
 */
public abstract class PMapTupleTable<TupleMapType, TupleType> implements TupleTable<TupleType> {

	/**
	 * We use an {@link AtomicReference} to the internal structure that holds our index data to be able to swap
	 * transactional versions of the index data with the shared version atomically.
	 */
	protected abstract AtomicReference<TupleMapType> master();

	private final ThreadLocal<TupleMapType> local = withInitial(() -> master().get());

	protected ThreadLocal<TupleMapType> local() {
		return local;
	}

	private final ThreadLocal<Boolean> isInTransaction = withInitial(() -> false);

	private final String name;

	public PMapTupleTable(final String n) {
		this.name = n;
	}

	protected abstract Logger log();

	protected void debug(final String msg, final Object... values) {
		log().debug(name + ": " + msg, values);
	}

	@Override
	public void begin(final ReadWrite rw) {
		isInTransaction.set(true);
	}

	@Override
	public void end() {
		debug("Abandoning transactional reference.");
		local.remove();
		isInTransaction.set(false);
	}

	@Override
	public void commit() {
		debug("Swapping transactional reference in for shared reference");
		master().set(local.get());
		end();
	}

	@Override
	public boolean isInTransaction() {
		return isInTransaction.get();
	};
}

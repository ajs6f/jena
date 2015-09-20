package org.apache.jena.shared;

import java.util.concurrent.locks.ReentrantLock;

public class MRPlusSWLock extends ReentrantLock implements Lock {

	@Override
	public void enterCriticalSection(final boolean readLockRequested) {
		if (!readLockRequested) lock();
	}

	@Override
	public void leaveCriticalSection() {
		if (isHeldByCurrentThread()) unlock();
	}
}

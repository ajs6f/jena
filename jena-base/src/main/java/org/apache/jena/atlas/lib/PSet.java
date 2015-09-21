package org.apache.jena.atlas.lib;

import java.util.stream.Stream;

import org.pcollections.Empty;

public class PSet<E> implements PersistentSet<E> {

	private final org.pcollections.PSet<E> wrappedSet;

	public static <E> PSet<E> empty() {
		return new PSet<>(Empty.set());
	}

	/**
	 * @param wrappedSet
	 */
	private PSet(final org.pcollections.PSet<E> w) {
		this.wrappedSet = w;
	}

	@Override
	public PersistentSet<E> plus(final E e) {
		return new PSet<>(wrappedSet.plus(e));
	}

	@Override
	public PersistentSet<E> minus(final E e) {
		return new PSet<>(wrappedSet.minus(e));
	}

	@Override
	public boolean contains(final E e) {
		return wrappedSet.contains(e);
	}

	@Override
	public Stream<E> stream() {
		return wrappedSet.stream();
	}
}

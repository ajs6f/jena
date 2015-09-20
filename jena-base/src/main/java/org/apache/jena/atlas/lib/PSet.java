package org.apache.jena.atlas.lib;

import java.util.function.Consumer;
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
	public PSet(final org.pcollections.PSet<E> wrappedSet) {
		this.wrappedSet = wrappedSet;
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

	@Override
	public void forEach(final Consumer<E> consumer) {
		wrappedSet.forEach(consumer);
	}
}

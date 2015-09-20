package org.apache.jena.atlas.lib;

import java.util.Map;
import java.util.stream.Stream;

public class PSet<E> implements PersistentSet<E> {

	private final PersistentMap<E, Object, ?> wrappedMap;

	private static final Object token = new Object();

	public static <E> PSet<E> empty() {
		return new PSet<>(PersistentMap.empty());
	}

	/**
	 * @param wrappedSet
	 */
	public PSet(final PersistentMap<E, Object, ?> wrapped) {
		this.wrappedMap = wrapped;
	}

	@Override
	public PersistentSet<E> plus(final E e) {
		return new PSet<>(wrappedMap.plus(e, token));
	}

	@Override
	public PersistentSet<E> minus(final E e) {
		return new PSet<>(wrappedMap.minus(e));
	}

	@Override
	public boolean contains(final E e) {
		return wrappedMap.containsKey(e);
	}

	@Override
	public Stream<E> stream() {
		return wrappedMap.entrySet().stream().map(Map.Entry::getKey);
	}
}

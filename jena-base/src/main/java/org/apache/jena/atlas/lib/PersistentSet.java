package org.apache.jena.atlas.lib;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface PersistentSet<E> {

	static <T> PersistentSet<T> empty() {
		return PSet.empty();
	}

	PersistentSet<E> plus(E e);

	PersistentSet<E> minus(E e);

	boolean contains(E e);

	Stream<E> stream();

	void forEach(Consumer<E> consumer);

}

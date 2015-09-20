package org.apache.jena.sparql.core.mem;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.pcollections.Empty;

public interface PersistentSet<E> {

	@SuppressWarnings("unchecked")
	static <T> PersistentSet<T> empty() {
		return (PersistentSet<T>) Empty.set();
	}

	PersistentSet<E> plus(E e);

	PersistentSet<E> minus(E e);

	boolean contains(E e);

	Stream<E> stream();

	void forEach(Consumer<E> consumer);

}

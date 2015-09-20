package org.apache.jena.atlas.lib;

import java.util.Map.Entry;
import java.util.Set;

import org.pcollections.Empty;

public class PMap<K, V> implements PersistentMap<K, V, PMap<K,V>> {

	private final org.pcollections.PMap<K, V> wrappedMap;

	/**
	 * @param wrappedMap
	 */
	PMap(final org.pcollections.PMap<K, V> wrappedMap) {
		this.wrappedMap = wrappedMap;
	}


	PMap() {
		this(Empty.map());
	}

	@Override
	public V get(final K key) {
		return wrappedMap.get(key);
	}

	@Override
	public PMap<K, V> plus(final K key, final V value) {
		return new PMap<>(wrappedMap.plus(key, value));
	}

	@Override
	public PMap<K, V> minus(final K key) {
		return new PMap<>(wrappedMap.minus(key));
	}

	@Override
	public boolean containsKey(final K key) {
		return wrappedMap.containsKey(key);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return wrappedMap.entrySet();
	}
}

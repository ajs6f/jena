package org.apache.jena.atlas.lib;

import java.util.Map.Entry;
import java.util.Set;

import org.pcollections.Empty;

public abstract class PMap<K, V, SelfType extends PMap<K,V, SelfType>> implements PersistentMap<K, V, SelfType> {

	protected final org.pcollections.PMap<K, V> wrappedMap;

	/**
	 * @param wrappedMap
	 */
	protected PMap(final org.pcollections.PMap<K, V> wrappedMap) {
		this.wrappedMap = wrappedMap;
	}


	protected PMap() {
		this(Empty.map());
	}

	@Override
	public V get(final K key) {
		return wrappedMap.get(key);
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

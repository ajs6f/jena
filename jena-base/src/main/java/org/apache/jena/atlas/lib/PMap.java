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

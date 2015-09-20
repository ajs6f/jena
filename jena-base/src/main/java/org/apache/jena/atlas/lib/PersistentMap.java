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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A <a href="https://en.wikipedia.org/wiki/Persistent_data_structure">persistent</a> map data structure.
 *
 */
public interface PersistentMap<K, V, SelfType extends PersistentMap<K, V, SelfType>> {


	public static <K, V> PersistentMap<K, V, PMap<K,V>> empty() {
		return new PMap<>();
	}

	V get(K key);

	SelfType plus(K key, V value);

	SelfType minus(K key);

	boolean containsKey(K key);

	Set<Map.Entry<K, V>> entrySet();

	default <R> Stream<R> descend(final Function<Entry<K, V>, Stream<R>> f) {
		return entrySet().stream().flatMap(f);
	}

}

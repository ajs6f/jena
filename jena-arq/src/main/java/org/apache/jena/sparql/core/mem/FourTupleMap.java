package org.apache.jena.sparql.core.mem;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;
import org.pcollections.Empty;
import org.pcollections.PMap;

public interface FourTupleMap extends PMap<Node, ThreeTupleMap> {

	static FourTupleMap empty() {
		return (FourTupleMap) Empty.<Node, ThreeTupleMap> map();
	}

	@Override
	FourTupleMap plus(Node key, ThreeTupleMap value);

	@Override
	FourTupleMap minus(Object key);

	public static interface ThreeTupleMap extends PMap<Node, TwoTupleMap> {

		static ThreeTupleMap empty() {
			return (ThreeTupleMap) Empty.<Node, TwoTupleMap> map();
		}

		@Override
		ThreeTupleMap plus(Node key, TwoTupleMap value);

		@Override
		ThreeTupleMap minus(Object key);
	}

	public static interface TwoTupleMap extends PMap<Node, PersistentSet<Node>> {

		static TwoTupleMap empty() {
			return (TwoTupleMap) Empty.<Node, PersistentSet<Node>> map();
		}

		@Override
		TwoTupleMap plus(Node key, PersistentSet<Node> value);

		@Override
		TwoTupleMap minus(Object key);
	}
}

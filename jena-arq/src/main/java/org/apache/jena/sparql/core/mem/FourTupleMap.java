package org.apache.jena.sparql.core.mem;

import org.apache.jena.atlas.lib.PMap;
import org.apache.jena.atlas.lib.PersistentMap;
import org.apache.jena.atlas.lib.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;

public interface FourTupleMap extends PersistentMap<Node, ThreeTupleMap, FourTupleMap> {

	static FourTupleMap empty() {
		return (FourTupleMap) (PersistentMap) PMap.<Node, ThreeTupleMap> empty();
	}

	public static interface ThreeTupleMap extends PersistentMap<Node, TwoTupleMap, ThreeTupleMap> {

		static ThreeTupleMap empty() {
			return (ThreeTupleMap) (PersistentMap) PMap.<Node, TwoTupleMap> empty();
		}
	}

	public static interface TwoTupleMap extends PersistentMap<Node, PersistentSet<Node>, TwoTupleMap> {

		static TwoTupleMap empty() {
			return (TwoTupleMap) (PersistentMap) PMap.<Node, PersistentSet<Node>> empty();
		}
	}
}

package org.apache.jena.sparql.core.mem;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.PersistentMap;
import org.apache.jena.atlas.lib.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;

public interface FourTupleMap extends PersistentMap<Node, ThreeTupleMap, FourTupleMap> {

	static FourTupleMap empty() {
		return (FourTupleMap) (PersistentMap) PersistentMap.empty();
	}

	default <R> Stream<R> descend(final BiFunction<Node, ThreeTupleMap, Stream<R>> f) {
		return descend(e -> f.apply(e.getKey(),e.getValue()));
	}

	public static interface ThreeTupleMap extends PersistentMap<Node, TwoTupleMap, ThreeTupleMap> {

		static ThreeTupleMap empty() {
			return (ThreeTupleMap) (PersistentMap) PersistentMap.empty();
		}

		default <R> Stream<R> descend(final BiFunction<Node, TwoTupleMap, Stream<R>> f) {
			return descend(e -> f.apply(e.getKey(),e.getValue()));
		}
	}

	public static interface TwoTupleMap extends PersistentMap<Node, PersistentSet<Node>, TwoTupleMap> {

		static TwoTupleMap empty() {
			return (TwoTupleMap) (PersistentMap) PersistentMap.empty();
		}

		default <R> Stream<R> descend(final BiFunction<Node, PersistentSet<Node>, Stream<R>> f) {
			return descend(e -> f.apply(e.getKey(),e.getValue()));
		}
	}
}

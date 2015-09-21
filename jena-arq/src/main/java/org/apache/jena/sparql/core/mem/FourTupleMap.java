package org.apache.jena.sparql.core.mem;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.PMap;
import org.apache.jena.atlas.lib.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;

public class FourTupleMap extends PMap<Node, ThreeTupleMap, FourTupleMap> {

	private FourTupleMap(final org.pcollections.PMap<Node, ThreeTupleMap> wrappedMap) {
		super(wrappedMap);
	}

	private FourTupleMap() {
		super();
	}

	@Override
	public FourTupleMap plus(final Node key, final ThreeTupleMap value) {
		return new FourTupleMap(wrappedMap.plus(key, value));
	}

	@Override
	public FourTupleMap minus(final Node key) {
		return new FourTupleMap(wrappedMap.minus(key));
	}

	static FourTupleMap empty() {
		return new FourTupleMap();
	}

	public <R> Stream<R> descend(final BiFunction<Node, ThreeTupleMap, Stream<R>> f) {
		return descend(e -> f.apply(e.getKey(), e.getValue()));
	}

	public static class ThreeTupleMap extends PMap<Node, TwoTupleMap, ThreeTupleMap> {
		private ThreeTupleMap(final org.pcollections.PMap<Node, TwoTupleMap> wrappedMap) {
			super(wrappedMap);
		}

		private ThreeTupleMap() {
			super();
		}

		@Override
		public ThreeTupleMap plus(final Node key, final TwoTupleMap value) {
			return new ThreeTupleMap(wrappedMap.plus(key, value));
		}

		@Override
		public ThreeTupleMap minus(final Node key) {
			return new ThreeTupleMap(wrappedMap.minus(key));
		}

		static ThreeTupleMap empty() {
			return new ThreeTupleMap();
		}

		public <R> Stream<R> descend(final BiFunction<Node, TwoTupleMap, Stream<R>> f) {
			return descend(e -> f.apply(e.getKey(), e.getValue()));
		}
	}

	public static class TwoTupleMap extends PMap<Node, PersistentSet<Node>, TwoTupleMap> {

		private TwoTupleMap(final org.pcollections.PMap<Node, PersistentSet<Node>> wrappedMap) {
			super(wrappedMap);
		}

		private TwoTupleMap() {
			super();
		}

		@Override
		public TwoTupleMap plus(final Node key, final PersistentSet<Node> value) {
			return new TwoTupleMap(wrappedMap.plus(key, value));
		}

		@Override
		public TwoTupleMap minus(final Node key) {
			return new TwoTupleMap(wrappedMap.minus(key));
		}

		static TwoTupleMap empty() {
			return new TwoTupleMap();
		}

		public <R> Stream<R> descend(final BiFunction<Node, PersistentSet<Node>, Stream<R>> f) {
			return descend(e -> f.apply(e.getKey(), e.getValue()));
		}
	}

}

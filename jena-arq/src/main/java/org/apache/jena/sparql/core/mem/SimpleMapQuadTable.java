package org.apache.jena.sparql.core.mem;

import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.sparql.core.Quad.create;
import static org.apache.jena.sparql.core.Quad.unionGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.tuple.TConsumer4;
import org.apache.jena.atlas.lib.tuple.TFunction4;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

public class SimpleMapQuadTable extends SimpleMapTupleTable<Map<Node, Map<Node, Set<Node>>>, Quad, TConsumer4<Node>>
        implements QuadTable {

    private Map<Node, Map<Node, Map<Node, Set<Node>>>> table = new DefaultingHashMap<Node, Map<Node, Map<Node, Set<Node>>>>(
            k1 -> new DefaultingHashMap<Node, Map<Node, Set<Node>>>(
                    k2 -> new DefaultingHashMap<Node, Set<Node>>(k3 -> new HashSet<>())));

    @Override
    protected Map<Node, Map<Node, Map<Node, Set<Node>>>> table() {
        return table;
    }

    public static Function<QuadTableForm, QuadTable> constructor = form -> form.name().startsWith("G")
            ? new SimpleMapQuadTableStartingWithGraph(form)
            : form.name().endsWith("G") ? new SimpleMapQuadTableEndingWithGraph(form) : new SimpleMapQuadTable(form);

    public SimpleMapQuadTable(final QuadTableForm order) {
        this("GSPO", order.name());
    }

    /**
     * @param canonical the canonical order outside this table
     * @param order the internal order for this table
     */
    public SimpleMapQuadTable(final String canonical, final String order) {
        this(canonical + "->" + order, TupleMap.create(canonical, order));
    }

    /**
     * @param tableName a name for this table
     * @param order the order of elements in this table
     */
    public SimpleMapQuadTable(final String tableName, final TupleMap order) {
        super(tableName, order);
    }

    @Override
    public void add(final Quad q) {
        map(add()).accept(q);
    }

    @Override
    public void delete(final Quad q) {
        map(delete()).accept(q);
    }

    @Override
    protected TConsumer4<Node> add() {
        return (first, second, third, fourth) -> {
            table().getOrDefault(first, new HashMap<>()).getOrDefault(second, new HashMap<>())
                    .getOrDefault(third, new HashSet<>()).add(fourth);
        };
    }

    @Override
    protected TConsumer4<Node> delete() {
        return (first, second, third, fourth) -> {
            if (table().containsKey(first)) {
                final Map<Node, Map<Node, Set<Node>>> threetuples = table().get(first);
                if (threetuples.containsKey(second)) {
                    final Map<Node, Set<Node>> twotuples = threetuples.get(second);
                    if (twotuples.containsKey(third)) {
                        final Set<Node> fourths = twotuples.get(fourth);
                        if (fourths.remove(fourth) && fourths.isEmpty()) {
                            twotuples.remove(third);
                            if (twotuples.isEmpty()) {
                                threetuples.remove(second);
                                if (threetuples.isEmpty()) table().remove(first);
                            }
                        }
                    }
                }
            }
        };
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        return map(find).apply(g, s, p, o);
    }

    private TFunction4<Node, Stream<Quad>> find = (first, second, third, fourth) -> {
        if (first != null && first.isConcrete()) {
            // concrete value for first slot
            final Map<Node, Map<Node, Set<Node>>> threetuples = table().get(first);
            if (second != null && second.isConcrete()) {
                // concrete value for second slot
                final Map<Node, Set<Node>> twotuples = threetuples.get(second);
                if (third != null && third.isConcrete()) {
                    // concrete value for third slot
                    if (fourth != null && fourth.isConcrete())
                        // concrete value for fourth slot
                        return Stream.of(unmap(first, second, third, fourth));
                    // wildcard for fourth slot
                    return twotuples.get(third).stream().map(slot4 -> unmap(first, second, third, slot4));
                }
                // wildcard for third slot
                return threetuples.get(second).entrySet().stream()
                        .flatMap(e -> e.getValue().stream().map(slot4 -> unmap(first, second, e.getKey(), slot4)));
            }
            // wildcard for second slot
            return threetuples.entrySet().stream().flatMap(e -> e.getValue().entrySet().stream()
                    .flatMap(e1 -> e1.getValue().stream().map(slot4 -> unmap(first, e.getKey(), e1.getKey(), slot4))));
        }
        // wildcard for first slot
        return table().entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream()
                        .flatMap(e1 -> e1.getValue().entrySet().stream().flatMap(e2 -> e2.getValue().stream()
                                .map(slot4 -> unmap(e.getKey(), e1.getKey(), e2.getKey(), slot4)))));
    };

    public static class SimpleMapQuadTableStartingWithGraph extends SimpleMapQuadTable {

        public SimpleMapQuadTableStartingWithGraph(QuadTableForm order) {
            super(order);
        }

        @Override
        public Stream<Node> listGraphNodes() {
            return table().entrySet().stream().map(Entry::getKey);
        }
    }

    public static class SimpleMapQuadTableEndingWithGraph extends SimpleMapQuadTable {

        public SimpleMapQuadTableEndingWithGraph(QuadTableForm order) {
            super(order);
        }

        @Override
        public Stream<Quad> findInUnionGraph(final Node s, final Node p, final Node o) {
            final AtomicReference<Triple> mostRecentlySeen = new AtomicReference<>();
            return find(ANY, s, p, o).map(Quad::asTriple).filter(t -> !mostRecentlySeen.getAndSet(t).equals(t))
                    .map(t -> create(unionGraph, t));
        }
    }
}

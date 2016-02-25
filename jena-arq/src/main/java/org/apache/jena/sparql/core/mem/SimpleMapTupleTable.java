package org.apache.jena.sparql.core.mem;

import java.util.Map;

import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.TransactionalNotSupportedMixin;

public abstract class SimpleMapTupleTable<TupleMapType, TupleType, ConsumerType>
extends OrderedTupleTable<TupleType, ConsumerType> implements TupleTable<TupleType>, TransactionalNotSupportedMixin {

    private String tableName;

    /**
     * @param n a name for this table
     */
    public SimpleMapTupleTable(final String n, final TupleMap order) {
        super(order);
        this.tableName = n;
    }

    @Override
    public void abort() {
        TransactionalNotSupportedMixin.super.abort();
    }
    
    protected abstract Map<Node, TupleMapType> table();

}

package org.apache.jena.sparql.core.mem;

import org.apache.jena.sparql.core.AbstractDatasetGraphTests;
import org.apache.jena.sparql.core.DatasetGraph;

public class TestDatasetGraphInMemoryWithSimpleImpls extends AbstractDatasetGraphTests {

    @Override
    protected DatasetGraph emptyDataset() {
        return new DatasetGraphInMemory(new HexTable(SimpleMapQuadTable.constructor), new TriTable(SimpleMapTripleTable::new) );
    }

}

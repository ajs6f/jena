package org.apache.jena.sparql.core.mem;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.AbstractDatasetGraphTests;
import org.apache.jena.sparql.core.AbstractTestGraphOverDataset;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemory.TestDatasetGraphInMemoryBasic;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemory.TestDatasetGraphInMemoryViews;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestDatasetGraphInMemoryBasic.class, TestDatasetGraphInMemoryViews.class })
public class TestDatasetGraphInMemory {

	public static class TestDatasetGraphInMemoryBasic extends AbstractDatasetGraphTests {
		@Override
		protected DatasetGraph emptyDataset() {
			return new DatasetGraphInMemory();
		}
	}

	public static class TestDatasetGraphInMemoryViews extends AbstractTestGraphOverDataset {

		@Override
		protected DatasetGraph createBaseDSG() {
			return new DatasetGraphInMemory();
		}

		@Override
		protected Graph makeNamedGraph(final DatasetGraph dsg, final Node gn) {
			return dsg.getGraph(gn);
		}

		@Override
		protected Graph makeDefaultGraph(final DatasetGraph dsg) {
			return dsg.getDefaultGraph();
		}
	}
}

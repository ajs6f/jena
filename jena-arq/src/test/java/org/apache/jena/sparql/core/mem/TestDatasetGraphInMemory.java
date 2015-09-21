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

package org.apache.jena.sparql.core.mem;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.AbstractDatasetGraphTests;
import org.apache.jena.sparql.core.AbstractTestGraphOverDataset;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.TestDatasetGraphWithLock;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemory.TestDatasetGraphInMemoryBasic;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemory.TestDatasetGraphInMemoryLock;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemory.TestDatasetGraphInMemoryViews;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestDatasetGraphInMemoryBasic.class, TestDatasetGraphInMemoryViews.class, TestDatasetGraphInMemoryLock.class })
public class TestDatasetGraphInMemory {

	public static class TestDatasetGraphInMemoryBasic extends AbstractDatasetGraphTests {
		@Override
		protected DatasetGraph emptyDataset() {
			return new DatasetGraphInMemory();
		}
	}

	public static class TestDatasetGraphInMemoryLock extends TestDatasetGraphWithLock {
	    @Override
	    protected Dataset createFixed() {
	        return DatasetFactory.create(new DatasetGraphInMemory());
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

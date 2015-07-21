package org.apache.jena.sparql.core.journaling;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestQuadOperation.class, TestListBackedOperationRecord.class, TestDatasetGraphWithRecord.class })
public class TestJournaling {}

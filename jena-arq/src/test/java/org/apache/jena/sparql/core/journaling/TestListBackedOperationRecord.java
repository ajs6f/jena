package org.apache.jena.sparql.core.journaling;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.jena.sparql.core.journaling.Operation.InvertibleOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestListBackedOperationRecord extends Assert {

	private static interface MockOp extends InvertibleOperation<Object, Object> {
	}

	@Mock
	private MockOp mockOp1, mockOp2, mockOp3;
	private List<MockOp> ops;

	@Before
	public void setup() {
		ops = new ArrayList<>(asList(mockOp1, mockOp2, mockOp3));
	}

	@Test
	public void testAdd() {
		final List<MockOp> results = new ArrayList<>();
		final ListBackedOperationRecord<MockOp> testRecord = new ListBackedOperationRecord<>(results);
		testRecord.add(mockOp1);
		testRecord.add(mockOp2);
		testRecord.add(mockOp3);
		assertEquals(3, results.size());
		assertEquals(mockOp1, results.get(0));
		assertEquals(mockOp2, results.get(1));
		assertEquals(mockOp3, results.get(2));
	}

	@Test
	public void testConsume() {
		final ListBackedOperationRecord<MockOp> testRecord = new ListBackedOperationRecord<>(ops);
		testRecord.consume(Objects::toString); // /dev/null
		assertTrue(ops.isEmpty());
	}

	@Test
	public void testClear() {
		final ListBackedOperationRecord<MockOp> testRecord = new ListBackedOperationRecord<>(ops);
		testRecord.clear();
		assertTrue(ops.isEmpty());
	}

	@Test
	public void testReverse() {
		final ListBackedOperationRecord<MockOp> testRecordReversed = new ListBackedOperationRecord<>(ops).reverse();
		final List<MockOp> results = new ArrayList<>();
		testRecordReversed.consume(results::add);
		assertEquals(mockOp3, results.get(0));
		assertEquals(mockOp2, results.get(1));
		assertEquals(mockOp1, results.get(2));
	}
}

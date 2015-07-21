package org.apache.jena.sparql.core.journaling;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
		ListBackedOperationRecord<MockOp> testRecord = new ListBackedOperationRecord<>(ops);
		testRecord.consume(op -> {}); // /dev/null
		assertTrue(ops.isEmpty());
		// now let's try with a problematic consumer
		ops = new ArrayList<>(asList(mockOp1, mockOp2, mockOp3));
		when(mockOp3.inverse()).thenThrow(new RuntimeException("Expected."));
		testRecord = new ListBackedOperationRecord<>(ops);
		try {
			testRecord.consume(MockOp::inverse);
		} catch (final Exception e) { // should not have been able to consume the last op, which threw an exception
			assertEquals("Expected.", e.getMessage());
			// should be one op left
			assertEquals(1, ops.size());
			// and it should be mockOp3
			assertEquals(mockOp3, ops.get(0));
		}
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

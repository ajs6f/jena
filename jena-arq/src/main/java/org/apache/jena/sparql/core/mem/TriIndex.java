package org.apache.jena.sparql.core.mem;

import static java.lang.ThreadLocal.withInitial;
import static java.util.EnumSet.noneOf;
import static java.util.stream.Collectors.toMap;
import static org.apache.jena.sparql.core.mem.Slot.*;
import static org.apache.jena.sparql.core.mem.TripleIndexForm.chooseFrom;
import static org.apache.jena.sparql.core.mem.TripleIndexForm.indexForms;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;

public class TriIndex implements TripleTable {

	private final Map<TripleIndexForm, TripleTable> indexBlock = new EnumMap<TripleIndexForm, TripleTable>(
			indexForms().collect(toMap(x -> x, TripleIndexForm::get)));

	private final ThreadLocal<Boolean> isInTransaction = withInitial(() -> false);

	@Override
	public void begin(final ReadWrite readWrite) {
		begin();
	}

	@Override
	public void commit() {
		indexBlock.values().forEach(TripleTable::commit);
		end();
	}

	@Override
	public void abort() {
		indexBlock.values().forEach(TripleTable::abort);
		end();
	}

	@Override
	public boolean isInTransaction() {
		return isInTransaction.get();
	}

	@Override
	public void end() {
		indexBlock.values().forEach(TripleTable::end);
		isInTransaction.set(false);
	}

	@Override
	public Stream<Triple> find(final Node s, final Node p, final Node o) {
		final Set<Slot> pattern = noneOf(Slot.class);
		if (isConcrete(s)) pattern.add(SUBJECT);
		if (isConcrete(p)) pattern.add(PREDICATE);
		if (isConcrete(o)) pattern.add(OBJECT);
		final TripleIndexForm choice = chooseFrom(pattern);
		return indexBlock.get(choice).find(s, p, o);
	}

	private static boolean isConcrete(final Node n) {
		return n != null && n.isConcrete();
	}

	@Override
	public void add(final Triple t) {
		indexBlock.values().forEach(index -> index.add(t));
	}

	@Override
	public void delete(final Triple t) {
		indexBlock.values().forEach(index -> index.delete(t));
	}

	@Override
	public void begin() {
		isInTransaction.set(true);
		indexBlock.values().forEach(TripleTable::begin);
	}

}

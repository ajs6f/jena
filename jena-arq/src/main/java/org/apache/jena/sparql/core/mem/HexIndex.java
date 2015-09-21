package org.apache.jena.sparql.core.mem;

import static java.util.EnumSet.noneOf;
import static java.util.stream.Collectors.toMap;
import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;
import static org.apache.jena.sparql.core.mem.IndexForm.*;
import static org.apache.jena.sparql.core.mem.IndexForm.Slot.*;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.IndexForm.Slot;

public class HexIndex extends Index {

	private final Map<IndexForm, Index> indexBlock = new EnumMap<>(indexForms().collect(toMap(x -> x, IndexForm::get)));

	@Override
	public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o, final boolean searchDefault) {
		final Set<Slot> pattern = noneOf(IndexForm.Slot.class);
		if (isConcrete(g)) pattern.add(GRAPH);
		if (isConcrete(s)) pattern.add(SUBJECT);
		if (isConcrete(p)) pattern.add(PREDICATE);
		if (isConcrete(o)) pattern.add(OBJECT);
		final IndexForm choice = chooseFrom(pattern);
		return indexBlock.get(choice).find(searchDefault ? defaultGraphIRI : g, s, p, o, searchDefault);
	}

	private static boolean isConcrete(final Node n) {
		return n != null && n.isConcrete();
	}

	@Override
	public void add(final Quad q) {
		indexBlock.values().forEach(index -> index.add(q));
	}

	@Override
	public void delete(final Quad q) {
		indexBlock.values().forEach(index -> index.delete(q));
	}

	Iterator<Node> listGraphNodes() {
		return indexBlock.get(GSPO).local.get().entrySet().stream().map(Map.Entry::getKey).iterator();
	}

	@Override
	public void begin() {
		indexBlock.values().forEach(Index::begin);
	}

	@Override
	public void end() {
		indexBlock.values().forEach(Index::end);
	}

	@Override
	public void commit() {
		indexBlock.values().forEach(Index::commit);
	}
}

package org.apache.jena.sparql.core.mem;

import org.apache.jena.graph.Node_URI;

public interface GraphNode {

	public class GraphNodeURI extends Node_URI implements GraphNode {

		protected GraphNodeURI(String uri) {
			super(uri);
		}
	}
}

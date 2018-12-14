package org.uu.nl.analyze.bca.util.jena;

import org.apache.jena.graph.Node;

public enum NodeType {
	URI, BLANK, LITERAL, UNKNOWN, PREDICATE;
	public static NodeType fromNode(Node node) {
		if(node.isBlank())
			return BLANK;
		if(node.isURI())
			return URI;
		if(node.isLiteral())
			return LITERAL;
		return UNKNOWN;
	}
}
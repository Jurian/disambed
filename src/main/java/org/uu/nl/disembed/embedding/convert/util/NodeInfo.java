package org.uu.nl.embedding.convert.util;

import org.apache.jena.graph.Node;

/**
 * @author Jurian Baas
 */
public enum NodeInfo {

	LITERAL((byte)2),
	BLANK((byte)1),
	URI((byte)0);

	public final byte id;

	NodeInfo(byte id) {
		this.id = id;
	}

	public static NodeInfo fromByte(final byte b) {
		switch (b) {
			case 0: return NodeInfo.URI;
			case 1: return NodeInfo.BLANK;
			case 2: return NodeInfo.LITERAL;
		}
		throw new IllegalArgumentException();
	}

	public static int type2index(Node node) {
		if(node.isURI()) return NodeInfo.URI.id;
		else if (node.isBlank()) return NodeInfo.BLANK.id;
		else if (node.isLiteral()) return NodeInfo.LITERAL.id;
		else throw new IllegalArgumentException("Node " + node + " is not of type URI, blank or literal");
	}
}

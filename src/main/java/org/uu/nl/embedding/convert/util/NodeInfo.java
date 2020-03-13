package org.uu.nl.embedding.convert.util;

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
}

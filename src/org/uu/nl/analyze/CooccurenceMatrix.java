package org.uu.nl.analyze;

import org.uu.nl.analyze.bca.util.jena.NodeType;

public interface CooccurenceMatrix {
	public int vocabSize();
	public double max();
	public String[] getKeys();
	public String getKey(int index);
	public NodeType[] getTypes();
	public NodeType getType(int index);
	public int cIdx_I(int i);
	public int cIdx_J(int j);
	public double cIdx_C(int i);
	public int cooccurrenceCount();
	public int uriNodeCount();
	public int predicateNodeCount();
	public int blankNodeCount();
	public int literalNodeCount();
}

package org.uu.nl.embedding;

public interface CooccurenceMatrix {
	public int vocabSize();
	public double max();
	public String[] getKeys();
	public String getKey(int index);
	public byte[] getTypes();
	public byte getType(int index);
	public int cIdx_I(int i);
	public int cIdx_J(int j);
	public double cIdx_C(int i);
	public int cooccurrenceCount();
	public int uriNodeCount();
	public int predicateNodeCount();
	public int blankNodeCount();
	public int literalNodeCount();
}

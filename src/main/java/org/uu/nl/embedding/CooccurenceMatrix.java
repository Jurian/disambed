package org.uu.nl.embedding;

/**
 * @author Jurian Baas
 */
public interface CooccurenceMatrix {
	int vocabSize();
	double max();
	String[] getKeys();
	String getKey(int index);
	byte[] getTypes();
	 byte getType(int index);
	int cIdx_I(int i);
	int cIdx_J(int j);
	double cIdx_C(int i);
	int cooccurrenceCount();
	int uriNodeCount();
	int predicateNodeCount();
	int blankNodeCount();
	int literalNodeCount();
}

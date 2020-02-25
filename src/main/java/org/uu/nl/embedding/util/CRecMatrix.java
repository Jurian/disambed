package org.uu.nl.embedding.util;

/**
 * @author Jurian Baas
 */
public interface CRecMatrix {
	int vocabSize();
	double max();
	//String[] getKeys();
	String getKey(int index);
	//byte[] getTypes();
	byte getType(int index);
	int cIdx_I(int i);
	int cIdx_J(int j);
	float cIdx_C(int i);
	int coOccurrenceCount();
	int getNrOfVertices();
	void shuffle();
}

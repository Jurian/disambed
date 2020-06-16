package org.uu.nl.embedding.util;

/**
 * @author Jurian Baas
 */
public interface CoOccurrenceMatrix {

	int contextIndex2Focus(int i);
	int focusIndex2Context(int i);
	int nrOfContextVectors();
	int nrOfFocusVectors();
	double max();
	String getKey(int index);
	byte getType(int index);
	int cIdx_I(int i);
	int cIdx_J(int j);
	float cIdx_C(int i);
	int coOccurrenceCount();
	void shuffle();
}

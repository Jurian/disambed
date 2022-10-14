package org.uu.nl.disembed.embedding.bca;

/**
 * @author Jurian Baas
 */
public interface CoOccurrenceMatrix {

	int contextIndex2Focus(int i);
	int focusIndex2Context(int i);
	int nrOfContextVectors();
	int nrOfFocusVectors();
	float max();
	String getKey(int index);
	int cIdx_I(int i);
	int cIdx_J(int j);
	float cIdx_C(int i);
	int coOccurrenceCount();
	void shuffle();
	double calculateMemoryMegaBytes();
}

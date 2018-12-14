package org.uu.nl.analyze.glove;

import org.uu.nl.analyze.CooccurenceMatrix;
import org.uu.nl.analyze.Embedding;

public class GloveModel extends Embedding {

	private final double alpha;
	private final double xMax;
	private final int vocabSize;

	public GloveModel(int dimension, double xMax, double alpha, CooccurenceMatrix coMatrix) {
		super(dimension, coMatrix);
		this.vocabSize = coMatrix.vocabSize();
		// Used for the weight function in GloVe
		this.xMax = xMax;
		this.alpha = alpha;
	}
	
	public GloveModel(int dimension, CooccurenceMatrix coMatrix) {
		this(dimension, coMatrix.max(), 0.75, coMatrix);
	}

	public double getAlpha() {
		return alpha;
	}

	public double getxMax() {
		return xMax;
	}

	public int getVocabSize() {
		return vocabSize;
	}

}

package org.uu.nl.embedding.glove;

import org.uu.nl.embedding.util.CRecMatrix;
import org.uu.nl.embedding.util.Embedding;
import org.uu.nl.embedding.util.config.Configuration;

/**
 * @author Jurian Baas
 */
public class GloveModel extends Embedding {

	private final double alpha;
	private final double xMax;
	private final int vocabSize;

	private GloveModel(int dimension, double xMax, CRecMatrix coMatrix) {
		super(dimension, coMatrix);
		this.vocabSize = coMatrix.vocabSize();
		// Used for the weight function in GloVe
		this.xMax = xMax;
		this.alpha = 0.75;
	}

    public GloveModel(CRecMatrix coMatrix, Configuration config) {
        this(config.getDim(), coMatrix.max(), coMatrix);
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

	public void setDimension(int dimension) { this.dimension = dimension; }
}

package org.uu.nl.embedding.util;

import org.uu.nl.embedding.opt.Optimum;

/**
 * @author Jurian Baas
 */
public abstract class Embedding {
	
	protected int dimension;
	private final CoOccurrenceMatrix coMatrix;
	private Optimum optimum;
	
	protected Embedding(int dim, CoOccurrenceMatrix coMatrix) {
		this.coMatrix = coMatrix;
		this.dimension = dim;
	}

	public int getDimension() {
		return dimension;
	}

	public CoOccurrenceMatrix getCoMatrix() {
		return coMatrix;
	}

	public Optimum getOptimum() { return optimum; }

	public void setOptimum(Optimum optimum) {
		this.optimum = optimum;
	}

	public abstract void setDimension(int dimension);

}

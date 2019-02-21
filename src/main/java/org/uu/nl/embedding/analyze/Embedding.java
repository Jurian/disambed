package org.uu.nl.embedding.analyze;

import org.uu.nl.embedding.analyze.glove.opt.Optimum;
import org.uu.nl.embedding.ann.kdtree.KDTree;

import java.util.Map;


public abstract class Embedding {
	
	protected final int dimension;
	protected final CooccurenceMatrix coMatrix;
	protected Optimum optimum;
	protected Map<String, Integer> keys;
	protected KDTree<String> tree;
	
	public Embedding(int dim, CooccurenceMatrix coMatrix) {
		this.coMatrix = coMatrix;
		this.dimension = dim;
	}

	public int getDimension() {
		return dimension;
	}

	public CooccurenceMatrix getCoMatrix() {
		return coMatrix;
	}

	public Optimum getOptimum() {
		return optimum;
	}

	public void setOptimum(Optimum optimum) {
		this.optimum = optimum;
	}

	public Map<String, Integer> getKeys() {
		return keys;
	}

	public void setKeys(Map<String, Integer> keys) {
		this.keys = keys;
	}

	public KDTree<String> getTree() {
		return tree;
	}

	public void setTree(KDTree<String> tree) {
		this.tree = tree;
	}
}

package org.uu.nl.embedding.analyze.glove.opt;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;


public class Optimum {



	private double finalCost;
	private double[] result;
	private final int dimension;
	
	public Optimum(int dimension) {
		this.dimension = dimension;
	}
	
	public double[] getResult() {
		return result;
	}

	public void setResult(double[] result) {
		this.result = result;
	}

	public double getFinalCost() {
		return finalCost;
	}

	public void setFinalCost(double finalCost) {
		this.finalCost = finalCost;
	}
}

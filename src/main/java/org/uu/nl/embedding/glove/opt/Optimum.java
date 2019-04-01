package org.uu.nl.embedding.glove.opt;

/**
 * @author Jurian Baas
 */
public class Optimum {

	private double finalCost;
	private double[] result;

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

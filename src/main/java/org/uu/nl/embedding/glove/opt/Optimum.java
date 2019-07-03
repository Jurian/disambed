package org.uu.nl.embedding.glove.opt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jurian Baas
 */
public class Optimum {

	private double finalCost;
	private double[] result;
	private List<Double> costHistory = new ArrayList<>();

	public void addIntermediaryResult(double result) {
		costHistory.add(result);
	}

	public void printCostHistory() {
		for(double d : costHistory) {
			System.out.println(d);
		}
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

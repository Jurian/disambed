package org.uu.nl.embedding.opt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jurian Baas
 */
public class Optimum implements Iterable<Optimizer.EmbeddedEntity>{

	private double finalCost;
	private Iterator<Optimizer.EmbeddedEntity> resultIterator;
	private final List<Double> costHistory = new ArrayList<>();

	public void addIntermediaryResult(double result) {
		costHistory.add(result);
	}

	public void printCostHistory() {
		for(double d : costHistory) {
			System.out.println(d);
		}
	}

	public void setResultIterator(Iterator<Optimizer.EmbeddedEntity> resultIterator) {
		this.resultIterator = resultIterator;
	}

	public double getFinalCost() {
		return finalCost;
	}

	public void setFinalCost(double finalCost) {
		this.finalCost = finalCost;
	}

	@Override
	public Iterator<Optimizer.EmbeddedEntity> iterator() {
		return this.resultIterator;
	}


}

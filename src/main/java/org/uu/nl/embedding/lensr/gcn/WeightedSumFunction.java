package org.uu.nl.embedding.lensr.gcn;

import java.util.List;

/**
 * Calculates the weighted sums of the input neurons' outputs.
 * 
 * @author Euan Westenbroek, based on the blog post
 * of Daniela Kolarova
 *
 */
public final class WeightedSumFunction implements InputSummingFunction {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getOutput(List<NeuronConnector> inputConnections) {
		double weightedSum = 0d;
		
		for (NeuronConnector connection : inputConnections) {
			weightedSum += connection.getWeightedInput();
			}
		return weightedSum;
	}
}

package org.uu.nl.embedding.lensr.gcn;

import java.util.List;

/**
 * Represents the inputs summing part of a neuron also called signal collector.
 * 
 * @author Euan Westenbroek, based on the blog post
 * of Daniela Kolarova
 *
 */
public interface InputSummingFunction {
	
	/**
	 * Performs calculations based on the output values of the input neurons.
	 * 
	 * @param inputConnections
	 * 				neuron's input connections
	 * @return total input for the neuron having the input connections
	 */
	double getOutput(List<NeuronConnector> inputConnections);
}

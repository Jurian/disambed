package org.uu.nl.embedding.lensr.gcn;


/**
 * Neural networks activation function interface.
 * 
 * @author Euan Westenbroek, based on the blog post
 * of Daniela Kolarova
 *
 */
public interface ActivationFunction {

	
	/**
	 * Performs calculation based on the sum of input neurons output.
	 * 
	 * @param summedInput
	 * 			neuron's sum of outputs respectively inputs for the 
	 * 			connected neuron.
	 * @return Output's calculation based on the sum of inputs.
	 */
	double calculateOutput(double summedInput);
}

package org.uu.nl.embedding.lensr.gcn;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Euan Westenbroek, based on the blog post
 * of Daniela Kolarova.
 *
 */

public class Neuron {
	
	/**
	 * Object id.
	 */
	private String id;
	
	/**
	 * A list of the neuron's dendrites, or
	 * incoming connectors.
	 */
	private List<NeuronConnector> dendrites;
	
	/**
	 * A list of the neuron's axons, or
	 * outgoing connectors.
	 */
	private List<NeuronConnector> axons;
	
	/**
	 * Input summing function.
	 */
	private InputSummingFunction inputSummingFunction;
	
	/**
	 * Activation function for this neuron.
	 */
	private ActivationFunction activationFunction;
	
	
	public Neuron() {
		this.axons = new ArrayList<NeuronConnector>();
		this.dendrites = new ArrayList<NeuronConnector>();
		
	}
	
	/**
	 * Calculates the neuron's output.
	 * @return
	 */
	public double calculateOutput() {
	
		double totalInput = inputSummingFunction.getOutput(dendrites);
		
		return activationFunction.calculateOutput(totalInput);
	}

}

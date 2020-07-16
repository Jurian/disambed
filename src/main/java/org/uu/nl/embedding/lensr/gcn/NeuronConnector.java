package org.uu.nl.embedding.lensr.gcn;


/**
 * Represents a connection between two neurons an the associated weight.
 * 
 * @author Euan Westenbroek, based on the blog post
 * of Daniela Kolarova
 *
 */
public class NeuronConnector {
	
	/**
	 * From neuron for this connection (source neuron). This connection is
	 * output connection for from neuron.
	 */
	private Neuron sourceNeuron;

	/**
	 * To neuron for this connection (target neuron). This connection is
	 * input connection for to neuron.
	 */
	private Neuron targetNeuron;
	
	/**
	 * Connector's weight.
	 */
	private double weight;
	
	/**
	 * Creates a new connection between specified neurons with specified weight.
	 * @param fromNeuron
	 * 			Neuron to connect from.
	 * @param toNeuron
	 * 			Neuron to connect to.
	 * @param initWeight
	 * 			Double variable to initialize the weight with.
	 */
	public NeuronConnector(final Neuron fromNeuron, final Neuron toNeuron, final double initWeight) {
		this.sourceNeuron = fromNeuron;
		this.targetNeuron = toNeuron;
		this.weight = initWeight;
	}
	
	/**
	 * Creates a new connection between specified neurons with random weight.
	 * @param fromNeuron
	 * 			Neuron to connect from.
	 * @param toNeuron
	 * 			Neuron to connect to.
	 */
	public NeuronConnector(final Neuron fromNeuron, final Neuron toNeuron) {
		this(fromNeuron, toNeuron, Math.random());
	}
	
	/**
	 * Returns the weight of this connection.
	 * @return weight
	 */
	public double getWeight() {
		return this.weight;
	}
	
	/**
	 * The new weight of the connection to be set.
	 * @param newWeight
	 */
	public void setWeight(final double newWeight) {
		this.weight = newWeight;
	}
	
	/**
	 * Returns input of this connection - the activation function result
	 * calculated in the input neuron of this connection.
	 * 
	 * @return input received through this connection
	 */
	public double getInput() {
		return sourceNeuron.calculateOutput();
	}
	
	/**
	 * Returns the weighted input of this connection.
	 * @return the weighted input of this connection
	 */
	public double getWeightedInput() {
		return sourceNeuron.calculateOutput() * weight;
	}
	
	/**
	 * Gets the source neuron for this connection.
	 * @return source neuron of this connection
	 */
	public Neuron getSourceNeuron() {
		return this.sourceNeuron;
	}
	
	/**
	 * Gets the target neuron for this connection.
	 * @return target neuron of this connection
	 */
	public Neuron getTargteNeuron() {
		return this.targetNeuron;
	}

}

package org.uu.nl.embedding.lensr.gcn;

import java.util.List;

/**
 * Represents an artificial neural network with layers containing neurons.
 * 
 * @author Euan Westenbroek, based on the blog post
 * of Daniela Kolarova
 *
 */
public class NeuralNetwork {
	
	/**
	 * Neural network's identifier.
	 */
	private String id;
	
	/**
	 * Neural network's input layer.
	 */
	private NeuralLayer inputLayer;
	
	/**
	 * Neural network's hidden layer(s).
	 */
	private List<NeuralLayer> hiddenLayers;
	
	/**
	 * Neural network output layer.
	 */
	private NeuralLayer outputLater;
	
	
	/**
	 * Constructs a neural net with all layers present.
	 * 
	 * @param id
	 * 			Neural network's id to be set
	 * @param inputLayer
	 * 			Neural network's input layer to be set
	 * @param hiddenLayers
	 * 			Neural network's hidden layer(s) to be set
	 * @param outputLayer
	 * 			Neural network's output layer to be set
	 */
	public NeuralNetwork(final String id, final NeuralLayer inputLayer, final List<NeuralLayer> hiddenLayers, final NeuralLayer outputLayer) {
		this.id = id;
		this.inputLayer = inputLayer;
		this.hiddenLayers = hiddenLayers;
		this.outputLater = outputLayer;
	}
	
	public void forwardPropagation() {
		
	}
	
	public void backwardPropagation() {
		
	}

}

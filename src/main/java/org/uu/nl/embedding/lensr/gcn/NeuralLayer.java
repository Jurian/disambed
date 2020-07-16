package org.uu.nl.embedding.lensr.gcn;

import java.util.ArrayList;
import java.util.List;


/**
 * Neural networks can be composed of several linked layers, forming the
 * so-called multilayer networks. A layer can be defined as a set of neurons
 * comprising a single neural net's layer.
 * 
 * @author Euan Westenbroek, based on the blog post
 * of Daniela Kolarova
 *
 */
public class NeuralLayer {
	
	/**
	 * Layer's identifier.
	 */
	private String id;
	
	/**
	 * The collection of neurons in this layer.
	 */
	private List<Neuron> neurons;
	
	
	/**
	 * Constructor for an empty layer with an id.
	 * 
	 * @param id
	 * 			layer's identifier
	 */
	public NeuralLayer(final String id) {
		this.id = id;
		this.neurons = new ArrayList<Neuron>();
	}
	
	/**
	 * Constructor for a layer with a list of neurons and an id.
	 * 
	 * @param id
	 * 			layer's identifier
	 * @param neurons
	 * 			list of neurons to be added to the layer
	 */
	public NeuralLayer(final String id, final List<Neuron> neurons) {
		this.id = id;
		this.neurons = neurons;
	}

}

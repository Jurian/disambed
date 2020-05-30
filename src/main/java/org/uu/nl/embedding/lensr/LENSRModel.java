package org.uu.nl.embedding.lensr;


/**
 * Class for the LENSR model
 * 
 * @author Euan Westenbroek
 * @version 0.1
 * @since 27-05-2020
 */
public class LENSRModel {
	
	public void LENSRModel() {
		
		/* TODO: implement constructor parameters
		 * 		and initialization methods.
		 */
	}
	
	/*
	 * Below all sub-algorithms can be found
	 * Which, when combined, make up the 
	 * algorithm as a whole.
	 * 
	 */
	
	private void layerWisePropagationRule() {
		
		double[] embeddingZ; // Z(l) = learnt latent node embedding at specific layer
		int layer; // l = layer of the embedder network
		double[][] diagDegreeMatrix; // Dtilde = diagonal degree matrix 
		double[][] adjMatrix; // Atilde = adjacency matrix
		int[][] idMatrix; // I_N = identity matrix
		double[][] weightMatrix; // W(l) = layer-specific trainable weight matrix
		String activationFunc; // niet echt een string?
		
		Matrix mat = new Matrix();
	}
	
	private void semanticRegularization() {
		
	}
	
	private void heterogeneousNodeEmbedding() {
		
	}
	
	private void embeddingTrainer() {
		
	}
	
	private void targetModelLoss() {
		
	}
	
	private void logicLoss () {
		
	}

}

package org.uu.nl.embedding.lensr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Graph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.util.OpsMatrix;

import Jama.Matrix;

/**
 * Class for the LENSR model
 * 
 * @author Euan Westenbroek
 * @version 0.1
 * @since 27-05-2020
 */
public class LENSRModel {
	
	Matrix qEmbedder;
	
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
	

	// layerWisePropagationRule variables
	Matrix[] Z; // Z(l) = 1xn learnt latent node embedding at different layers
	Matrix D; // Dtilde = 2d diagonal degree matrix 
	Matrix A; // Atilde = 2d adjacency matrix
	Matrix I; // I_N = 2d identity matrix
	Matrix[] W; // W(l) = nx1 layer-specific trainable weight matrix
	/*
	 * Activation Function needs to get proper functions etc.
	 */
	String activationFunc; // niet echt een string?
	Map<String, Integer> activationFuncMap = new HashMap<String, Integer>();
	/*
	 * ******************************************************
	 */
	
	
	private void layerWisePropagationRule(final int layer) {
		
		Matrix poweredDTilde = OpsMatrix.fractionalPower(D, (-1), 2);
		I = Matrix.identity(A.getRowDimension(), A.getColumnDimension());
		Matrix aTilde = A.plus(I);
		
		Matrix resMatrix = poweredDTilde.times(aTilde);
		resMatrix = resMatrix.times(poweredDTilde);
		resMatrix = resMatrix.times(Z[layer]);
		resMatrix = resMatrix.times(W[layer]);
		
		//*******************************************************************
		resMatrix = resMatrix.times(activationFuncMap.get(activationFunc));
		//*******************************************************************
		
		Z[layer+1] = resMatrix;
	}

	// semanticRegularization variables
	ArrayList<Integer> orNodes;
	ArrayList<Integer> andNodes;
	InMemoryDdnnfGraph logicGraph;
	
	private void semanticRegularization(LogicRule F) {
		
		Matrix resLoss = new Matrix(Z[Z.length-1].getRowDimension(), 1);
		Matrix subSum  = new Matrix(Z[Z.length-1].getRowDimension(), 1);
		Matrix subResult = new Matrix(Z[Z.length-1].getRowDimension(), 1);
		Matrix matMultip;
		LogicRule[] allTerms;
		
		orNodes = getOrNodes(F);
		andNodes = getAndNodes(F);
		logicGraph = F.getDdnnfGraph();
		LogicRule rule;
		
		
		// OR-nodes regularization
		for(int v : orNodes) {
			subSum  = new Matrix(Z[Z.length-1].getRowDimension(), 1);
			rule = logicGraph.getIntLogicMap().get(v);
			allTerms = rule.getAllTerms();
			for(LogicRule vj : allTerms) {
				subSum = subSum.plus(embedLogic(vj) - 1);
			}
			// Get Euclidean distance
			subSum = Math.abs(subSum);  
			subResult = (result.ycoord)*(result.ycoord) + (result.xcoord)*(result.xcoord); // Not Math.sqrt() for squared Euclidean distance
		}
		resLoss = subResult;
		
		// AND-nodes regularization
		for(int v : orNodes) {
			subSum = new Matrix(Z[Z.length-1].getRowDimension(), 1);
			rule = logicGraph.getIntLogicMap().get(v);
			allTerms = rule.getAllTerms();
			for(LogicRule vj : allTerms) {
				matMultip = VertexMatrixK(vj).transpose().times(VertexMatrixK(vj));
				subSum = matMultip.minus(matMultip.arrayTimes(OpsMatrix.antiIdentity(matMultip)));
			}
			// Get Euclidean distance
			subSum = Math.abs(subSum);  
			subResult = (result.ycoord)*(result.ycoord) + (result.xcoord)*(result.xcoord); // Not Math.sqrt() for squared Euclidean distance
		}
		resLoss = resLoss.plus(subResult);
	}
	
	private Matrix embedLogic(LogicRule rule) {
		Matrix placeHolder = new Matrix(Z[Z.length-1].getRowDimension(), 1);;
		return placeHolder;
	}
	
	private Matrix VertexMatrixK(LogicRule rule) {
		Matrix placeHolder = new Matrix(Z[Z.length-1].getRowDimension(), 1);;
		return placeHolder;
	}

	private void heterogeneousNodeEmbedding() {
		
	}
	
	private void embeddingTrainer() {
		
	}
	
	private void targetModelLoss() {
		
	}
	
	private void logicLoss () {
		
	}
	
	private ArrayList<Integer> getAndNodes(LogicRule F) {
		List<Integer> andNodes = new ArrayList<Integer>();
		InMemoryDdnnfGraph logicGraph = F.getDdnnfGraph();
		
		for(Map.Entry<Integer, String> entry : logicGraph.getGraph().entrySet()) {
			if(entry.getValue() == "AND") {
				andNodes.add(entry.getKey());
			}
		}
		// Sorting
		Collections.sort(andNodes);
		return (ArrayList<Integer>) andNodes;// Gaat dit goed?
	}

	private ArrayList<Integer> getOrNodes(LogicRule F) {
		List<Integer> orNodes = new ArrayList<Integer>();
		InMemoryDdnnfGraph logicGraph = F.getDdnnfGraph();
		
		for(Map.Entry<Integer, String> entry : logicGraph.getGraph().entrySet()) {
			if(entry.getValue() == "OR") {
				orNodes.add(entry.getKey());
			}
		}
		// Sorting
		Collections.sort(orNodes);
		return (ArrayList<Integer>) orNodes;// Gaat dit goed?
	}

}
